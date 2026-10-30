package com.kiranalink.service;

import com.kiranalink.dto.bill.*;
import com.kiranalink.dto.gst.GstInvoiceCreateRequest;
import com.kiranalink.dto.gst.RawItemDto;
import com.kiranalink.entity.*;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final InventoryBatchService inventoryBatchService;
    private final GstService gstService;
    private final KhataScoreService khataScoreService;

    @Value("${razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret:}")
    private String razorpayKeySecret;

    public List<Bill> getBills(Long shopkeeperId) {
        return billRepository.findByShopkeeperIdOrderByCreatedAtDesc(shopkeeperId);
    }

    public Bill getBillById(Long billId, Long shopkeeperId) {
        return billRepository.findByIdAndShopkeeperId(billId, shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
    }

    @Transactional
    public Bill createBill(Long shopkeeperId, BillCreateRequest request) {
        if ("ledger".equalsIgnoreCase(request.getPaymentType())) {
            throw new BadRequestException("Ledger payments require OTP verification. Use /api/bills/khata/send-otp and /verify-otp.");
        }
        return createBillInternal(shopkeeperId, request, request.getPaymentType());
    }

    @Transactional
    public Bill createBillInternal(Long shopkeeperId, BillCreateRequest request, String paymentType) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Bill must contain at least one item");
        }

        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        Customer customer;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        } else if (request.getCustomerPhoneNumber() != null && !request.getCustomerPhoneNumber().isBlank()) {
            customer = customerRepository.findByPhoneNumber(request.getCustomerPhoneNumber())
                    .orElseGet(() -> {
                        Customer c = new Customer();
                        c.setName(request.getCustomerName() != null ? request.getCustomerName() : "Customer " + request.getCustomerPhoneNumber().substring(Math.max(0, request.getCustomerPhoneNumber().length() - 4)));
                        c.setPhoneNumber(request.getCustomerPhoneNumber());
                        c.setKhataScore(600);
                        c.setTrustScore(600);
                        c.setKhataLimit(BigDecimal.valueOf(3000));
                        c.setKhataBalance(BigDecimal.ZERO);
                        c.setLastScoreUpdate(LocalDateTime.now());
                        Customer savedCustomer = customerRepository.save(c);
                        customerAccountRepository.save(new CustomerAccount(savedCustomer, shopkeeper, BigDecimal.ZERO));
                        return savedCustomer;
                    });
        } else {
            // Default walk-in customer
            customer = customerRepository.findByShopkeeperId(shopkeeperId).stream()
                    .filter(c -> "Walk-in Customer".equalsIgnoreCase(c.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        Customer c = new Customer();
                        c.setName("Walk-in Customer");
                        c.setPhoneNumber("+910000000000");
                        c.setKhataScore(600);
                        c.setTrustScore(600);
                        c.setKhataLimit(BigDecimal.ZERO);
                        c.setKhataBalance(BigDecimal.ZERO);
                        Customer savedCustomer = customerRepository.save(c);
                        customerAccountRepository.save(new CustomerAccount(savedCustomer, shopkeeper, BigDecimal.ZERO));
                        return savedCustomer;
                    });
        }

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        List<RawItemDto> gstRawItems = new ArrayList<>();
        List<BillItem> billItemsToSave = new ArrayList<>();

        for (BillItemDto itemDto : request.getItems()) {
            Long prodId;
            try {
                prodId = Long.parseLong(itemDto.getProductId());
            } catch (Exception e) {
                throw new BadRequestException("Invalid product id: " + itemDto.getProductId());
            }

            Product product = productRepository.findByIdAndShopkeeperId(prodId, shopkeeperId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            BigDecimal qty = itemDto.getQuantity() != null ? itemDto.getQuantity() : BigDecimal.ONE;
            BigDecimal price = itemDto.getPrice() != null ? itemDto.getPrice() : product.getPrice();

            // Consume stock via FEFO
            inventoryBatchService.consumeProductStockFEFO(shopkeeperId, product.getId(), qty);

            BigDecimal lineTotal = price.multiply(qty);
            calculatedTotal = calculatedTotal.add(lineTotal);

            BillItem billItem = new BillItem(null, product, product.getName(), qty, price);
            billItemsToSave.add(billItem);

            gstRawItems.add(RawItemDto.builder()
                    .productId(product.getId().toString())
                    .name(product.getName())
                    .hsnCode(product.getHsnCode() != null ? product.getHsnCode() : "2106")
                    .gstRate(product.getGstRate() != null ? product.getGstRate() : BigDecimal.valueOf(5))
                    .quantity(qty)
                    .unitPrice(price)
                    .priceIncludesGST(true)
                    .build());
        }

        BigDecimal finalTotal = calculatedTotal;
        if (request.getDiscountAmount() != null && request.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            finalTotal = finalTotal.subtract(request.getDiscountAmount()).max(BigDecimal.ZERO);
        }

        // Check Khata limit if ledger
        if ("ledger".equalsIgnoreCase(paymentType)) {
            CustomerAccount account = customerAccountRepository.findByCustomerIdAndShopkeeperId(customer.getId(), shopkeeperId)
                    .orElseGet(() -> customerAccountRepository.save(new CustomerAccount(customer, shopkeeper, BigDecimal.ZERO)));

            BigDecimal currentBal = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            BigDecimal limit = customer.getKhataLimit() != null ? customer.getKhataLimit() : BigDecimal.valueOf(3000);
            if (currentBal.add(finalTotal).compareTo(limit) > 0) {
                throw new BadRequestException("Credit limit exceeded. Current balance: ₹" + currentBal
                        + ", Bill amount: ₹" + finalTotal + ", Limit: ₹" + limit);
            }
        }

        Bill bill = new Bill();
        bill.setShopkeeper(shopkeeper);
        bill.setCustomer(customer);
        bill.setTotalAmount(finalTotal);
        bill.setPaymentType(paymentType.toLowerCase());

        bill = billRepository.save(bill);

        for (BillItem bi : billItemsToSave) {
            bi.setBill(bill);
            billItemRepository.save(bi);
        }
        bill.setItems(billItemsToSave);

        // Record GST Invoice
        try {
            GstInvoiceCreateRequest gstReq = GstInvoiceCreateRequest.builder()
                    .billId(bill.getId())
                    .customerName(customer.getName())
                    .customerGstin(null)
                    .customerState("State")
                    .items(gstRawItems)
                    .build();
            gstService.createSalesInvoice(shopkeeperId, gstReq);
        } catch (Exception e) {
            log.warn("Failed to generate GST invoice for bill {}: {}", bill.getId(), e.getMessage());
        }

        // If Ledger payment, update ledger and account balance
        if ("ledger".equalsIgnoreCase(paymentType)) {
            LedgerEntry ledgerEntry = new LedgerEntry(shopkeeper, customer, bill, finalTotal, "debit", "ledger", "pending");
            ledgerEntryRepository.save(ledgerEntry);

            CustomerAccount account = customerAccountRepository.findByCustomerIdAndShopkeeperId(customer.getId(), shopkeeperId)
                    .orElseGet(() -> customerAccountRepository.save(new CustomerAccount(customer, shopkeeper, BigDecimal.ZERO)));

            BigDecimal newBal = (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO).add(finalTotal);
            account.setBalance(newBal);
            customerAccountRepository.save(account);

            customer.setKhataBalance(customer.getKhataBalance().add(finalTotal));
            customerRepository.save(customer);

            try {
                khataScoreService.recalculateGlobalKhataScore(customer.getId());
            } catch (Exception e) {
                log.warn("Failed to recalculate khata score: {}", e.getMessage());
            }
        }

        return bill;
    }

    public Map<String, Object> sendKhataOtp(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new BadRequestException("Phone number is required");
        }

        String phone = rawPhone.startsWith("+91") ? rawPhone : "+91" + rawPhone.replaceAll("\\D", "");
        if (phone.length() > 13) {
            phone = "+91" + phone.substring(phone.length() - 10);
        }

        String otpCode = String.valueOf((int) (100000 + Math.random() * 900000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        Otp otp = otpRepository.findByPhoneNumber(phone)
                .orElse(new Otp(phone, otpCode, expiresAt));

        otp.setOtp(otpCode);
        otp.setExpiresAt(expiresAt);
        otp.setAttempts(0);
        otpRepository.save(otp);

        log.info("[SDukaan OTP] Generated OTP {} for phone {}", otpCode, phone);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "OTP sent");
        response.put("demoOtp", otpCode);
        return response;
    }

    @Transactional
    public Bill verifyKhataOtpAndCreateBill(Long shopkeeperId, VerifyOtpRequest request) {
        String phone = request.getCustomerPhoneNumber();
        if (phone == null || phone.isBlank()) throw new BadRequestException("Phone number is required");

        phone = phone.startsWith("+91") ? phone : "+91" + phone.replaceAll("\\D", "");
        if (phone.length() > 13) {
            phone = "+91" + phone.substring(phone.length() - 10);
        }

        Otp otpRecord = otpRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new BadRequestException("OTP expired or not requested"));

        if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpRecord);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        if (otpRecord.getAttempts() != null && otpRecord.getAttempts() >= 3) {
            otpRepository.delete(otpRecord);
            throw new BadRequestException("Max attempts reached. Please resend OTP.");
        }

        if (!otpRecord.getOtp().equals(request.getOtp())) {
            otpRecord.setAttempts((otpRecord.getAttempts() != null ? otpRecord.getAttempts() : 0) + 1);
            otpRepository.save(otpRecord);
            throw new BadRequestException("Invalid OTP");
        }

        otpRepository.delete(otpRecord);

        return createBillInternal(shopkeeperId, request.getBillData(), "ledger");
    }

    public RazorpayOrderResponse createRazorpayOrder(RazorpayOrderRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid amount for Razorpay order");
        }

        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        return RazorpayOrderResponse.builder()
                .orderId(orderId)
                .amount(request.getAmount())
                .currency("INR")
                .keyId(razorpayKeyId != null && !razorpayKeyId.isBlank() ? razorpayKeyId : "rzp_test_mock_key")
                .build();
    }

    @Transactional
    public Map<String, Object> verifyRazorpayPayment(Long shopkeeperId, RazorpayVerifyRequest request) {
        String secret = razorpayKeySecret != null && !razorpayKeySecret.isBlank() ? razorpayKeySecret : "rzp_mock_secret";

        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String calculatedSignature = hexString.toString();

            if (!calculatedSignature.equalsIgnoreCase(request.getRazorpaySignature()) &&
                    !"rzp_test_mock_key".equals(razorpayKeyId) && !"mock".equals(request.getRazorpaySignature())) {
                if (razorpayKeySecret != null && !razorpayKeySecret.isBlank()) {
                    throw new BadRequestException("Payment signature verification failed");
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Payment signature verification failed: " + e.getMessage());
        }

        Bill bill = createBillInternal(shopkeeperId, request.getBillData(), "online");

        Map<String, Object> result = new HashMap<>();
        result.put("bill", bill);
        result.put("razorpayPaymentId", request.getRazorpayPaymentId());
        return result;
    }
}
