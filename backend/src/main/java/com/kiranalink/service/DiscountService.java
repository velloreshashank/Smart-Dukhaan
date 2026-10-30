package com.kiranalink.service;

import com.kiranalink.dto.discount.*;
import com.kiranalink.entity.*;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountUsageHistoryRepository discountUsageHistoryRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    private String generateDiscountCode(String productName, BigDecimal discountValue) {
        String clean = productName.replaceAll("[^a-zA-Z0-9]", "");
        String prefix = clean.length() >= 4 ? clean.substring(0, 4).toUpperCase() : clean.toUpperCase();
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMM", Locale.ENGLISH)).toUpperCase();
        int random = (int) (Math.random() * 90 + 10);
        return "EXP" + discountValue.intValue() + prefix + dateStr + random;
    }

    @Transactional
    public DiscountCode createDiscount(Long shopkeeperId, DiscountCreateRequest request) {
        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        Product product = null;
        String productName = "DISCOUNT";
        if (request.getProductId() != null) {
            product = productRepository.findByIdAndShopkeeperId(request.getProductId(), shopkeeperId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
            productName = product.getName();
        }

        String code = generateDiscountCode(productName, request.getDiscountValue());

        DiscountCode discount = new DiscountCode();
        discount.setCode(code);
        discount.setShopkeeper(shopkeeper);
        discount.setProduct(product);
        discount.setDescription(request.getDescription() != null ? request.getDescription() : "Special Discount");
        discount.setDiscountType(request.getDiscountType().toLowerCase());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setMinPurchase(request.getMinPurchase() != null ? request.getMinPurchase() : BigDecimal.ZERO);
        discount.setMaxUses(request.getMaxUses() != null ? request.getMaxUses() : 100);
        discount.setUsedCount(0);
        discount.setValidFrom(LocalDateTime.now());
        discount.setValidUntil(request.getValidUntil());
        discount.setIsActive(true);
        discount.setCreatedFor(request.getCreatedFor() != null ? request.getCreatedFor() : "manual");
        discount.setLinkedBatchId(request.getLinkedBatchId());

        return discountCodeRepository.save(discount);
    }

    public List<DiscountCode> getDiscounts(Long shopkeeperId, Boolean isActive, String createdFor) {
        List<DiscountCode> list = discountCodeRepository.findByShopkeeperIdOrderByCreatedAtDesc(shopkeeperId);

        return list.stream()
                .filter(d -> {
                    if (isActive != null && !isActive.equals(d.getIsActive())) return false;
                    if (createdFor != null && !createdFor.isBlank() && !createdFor.equalsIgnoreCase(d.getCreatedFor())) return false;
                    return true;
                })
                .toList();
    }

    @Transactional
    public DiscountCode updateDiscount(Long shopkeeperId, Long discountId, Boolean isActive, BigDecimal discountValue, Integer maxUses, LocalDateTime validUntil) {
        DiscountCode discount = discountCodeRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + discountId));

        if (!discount.getShopkeeper().getId().equals(shopkeeperId)) {
            throw new BadRequestException("Unauthorized access to this discount");
        }

        if (isActive != null) discount.setIsActive(isActive);
        if (discountValue != null) discount.setDiscountValue(discountValue);
        if (maxUses != null) discount.setMaxUses(maxUses);
        if (validUntil != null) discount.setValidUntil(validUntil);

        return discountCodeRepository.save(discount);
    }

    public DiscountValidateResponse validateDiscount(Long shopkeeperId, DiscountValidateRequest request) {
        String code = request.getCode().trim().toUpperCase();
        DiscountCode discount = discountCodeRepository.findByCodeAndShopkeeperId(code, shopkeeperId)
                .orElse(null);

        if (discount == null || Boolean.FALSE.equals(discount.getIsActive())) {
            return DiscountValidateResponse.builder()
                    .valid(false)
                    .message("Invalid discount code")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if ((discount.getValidFrom() != null && now.isBefore(discount.getValidFrom())) ||
                (discount.getValidUntil() != null && now.isAfter(discount.getValidUntil()))) {
            return DiscountValidateResponse.builder()
                    .valid(false)
                    .message("Discount code has expired")
                    .build();
        }

        if (discount.getUsedCount() >= discount.getMaxUses()) {
            return DiscountValidateResponse.builder()
                    .valid(false)
                    .message("Discount code usage limit reached")
                    .build();
        }

        BigDecimal billAmt = request.getBillAmount() != null ? request.getBillAmount() : BigDecimal.ZERO;
        if (discount.getMinPurchase() != null && billAmt.compareTo(discount.getMinPurchase()) < 0) {
            return DiscountValidateResponse.builder()
                    .valid(false)
                    .message("Minimum purchase of ₹" + discount.getMinPurchase() + " required")
                    .build();
        }

        BigDecimal discountAmt;
        if ("percentage".equalsIgnoreCase(discount.getDiscountType())) {
            discountAmt = billAmt.multiply(discount.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discountAmt = discount.getDiscountValue().min(billAmt);
        }

        return DiscountValidateResponse.builder()
                .valid(true)
                .discount(DiscountValidateResponse.DiscountInfo.builder()
                        .code(discount.getCode())
                        .description(discount.getDescription())
                        .discountType(discount.getDiscountType())
                        .discountValue(discount.getDiscountValue())
                        .discountAmount(discountAmt)
                        .build())
                .build();
    }

    @Transactional
    public DiscountCode applyDiscount(Long shopkeeperId, Long discountId, DiscountApplyRequest request) {
        DiscountCode discount = discountCodeRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + discountId));

        if (!discount.getShopkeeper().getId().equals(shopkeeperId)) {
            throw new BadRequestException("Unauthorized access to this discount");
        }

        discount.setUsedCount(discount.getUsedCount() + 1);
        discountCodeRepository.save(discount);

        BigDecimal discountAmt = discount.getDiscountValue();
        if (request.getBillAmount() != null && "percentage".equalsIgnoreCase(discount.getDiscountType())) {
            discountAmt = request.getBillAmount().multiply(discount.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        DiscountUsageHistory usage = new DiscountUsageHistory(discount, request.getBillId(), request.getCustomerId(), discountAmt);
        discountUsageHistoryRepository.save(usage);

        return discount;
    }

    public void sendDiscountToCustomer(Long shopkeeperId, Long discountId, Long customerId) {
        DiscountCode discount = discountCodeRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + discountId));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        log.info("[Discount WhatsApp Mock] Sending code {} to customer {} ({})",
                discount.getCode(), customer.getName(), customer.getPhoneNumber());
    }
}
