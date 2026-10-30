package com.kiranalink.service;

import com.kiranalink.dto.gst.GstPurchaseCreateRequest;
import com.kiranalink.dto.gst.RawItemDto;
import com.kiranalink.dto.supplier.SupplierBillProcessRequest;
import com.kiranalink.dto.supplier.SupplierLineItemDto;
import com.kiranalink.entity.Product;
import com.kiranalink.entity.SupplierBill;
import com.kiranalink.entity.SupplierBillItem;
import com.kiranalink.entity.User;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.ProductRepository;
import com.kiranalink.repository.SupplierBillItemRepository;
import com.kiranalink.repository.SupplierBillRepository;
import com.kiranalink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierBillService {

    private final SupplierBillRepository supplierBillRepository;
    private final SupplierBillItemRepository supplierBillItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryBatchService inventoryBatchService;
    private final GstService gstService;

    public List<SupplierBill> getBills(Long shopkeeperId) {
        return supplierBillRepository.findByShopkeeperIdOrderByCreatedAtDesc(shopkeeperId);
    }

    private double calculateStringSimilarity(String s1, String s2) {
        String str1 = s1.toLowerCase().replaceAll("[^a-z0-9]", "");
        String str2 = s2.toLowerCase().replaceAll("[^a-z0-9]", "");

        if (str1.equals(str2)) return 1.0;
        if (str1.contains(str2) || str2.contains(str1)) return 0.85;

        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i <= str1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= str2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        int maxLen = Math.max(str1.length(), str2.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - ((double) dp[str1.length()][str2.length()] / maxLen);
    }

    @Transactional
    public Map<String, Object> processSupplierBill(Long shopkeeperId, SupplierBillProcessRequest request) {
        if (request.getLineItems() == null || request.getLineItems().isEmpty()) {
            throw new BadRequestException("Line items cannot be empty");
        }

        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        List<Product> allProducts = productRepository.findByShopkeeperId(shopkeeperId);
        List<Map<String, Object>> itemResults = new ArrayList<>();
        List<SupplierBillItem> billItemsToSave = new ArrayList<>();
        List<RawItemDto> gstPurchaseItems = new ArrayList<>();
        BigDecimal totalBillAmount = BigDecimal.ZERO;

        for (SupplierLineItemDto item : request.getLineItems()) {
            BigDecimal qty = item.getQuantity();
            BigDecimal totalAmt = item.getTotalAmount();
            totalBillAmount = totalBillAmount.add(totalAmt);

            BigDecimal unitCost = qty.compareTo(BigDecimal.ZERO) > 0 ?
                    totalAmt.divide(qty, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            BigDecimal sellingPrice = item.getCustomSellingPrice() != null ?
                    item.getCustomSellingPrice() : unitCost.multiply(BigDecimal.valueOf(1.05)).setScale(2, RoundingMode.HALF_UP);

            Product matched = null;
            double bestScore = 0.0;
            for (Product p : allProducts) {
                double score = calculateStringSimilarity(item.getProductName(), p.getName());
                if (score > bestScore) {
                    bestScore = score;
                    matched = p;
                }
            }

            String action = "created";
            Product finalProduct;

            if (matched != null && bestScore >= 0.80) {
                action = "updated";
                matched.setCostPrice(unitCost);
                if (item.getCustomSellingPrice() != null) {
                    matched.setPrice(item.getCustomSellingPrice());
                }
                finalProduct = productRepository.save(matched);
            } else {
                finalProduct = new Product(
                        shopkeeper,
                        item.getProductName(),
                        sellingPrice,
                        BigDecimal.ZERO,
                        "Grocery",
                        item.getUnit() != null ? item.getUnit() : "unit",
                        "📦"
                );
                finalProduct.setCostPrice(unitCost);
                finalProduct = productRepository.save(finalProduct);
                allProducts.add(finalProduct);
            }

            inventoryBatchService.addStockBatch(
                    shopkeeperId,
                    finalProduct.getId(),
                    qty,
                    unitCost,
                    finalProduct.getPrice(),
                    item.getBatchCode(),
                    item.getMfgDate(),
                    item.getExpiryDate(),
                    null,
                    "supplier_bill"
            );

            gstPurchaseItems.add(RawItemDto.builder()
                    .productId(finalProduct.getId().toString())
                    .name(finalProduct.getName())
                    .hsnCode(finalProduct.getHsnCode() != null ? finalProduct.getHsnCode() : "2106")
                    .gstRate(finalProduct.getGstRate() != null ? finalProduct.getGstRate() : BigDecimal.valueOf(5))
                    .quantity(qty)
                    .unitPrice(unitCost)
                    .priceIncludesGST(true)
                    .build());

            SupplierBillItem bi = new SupplierBillItem(
                    null,
                    item.getProductName(),
                    qty,
                    item.getUnit() != null ? item.getUnit() : "unit",
                    totalAmt,
                    unitCost,
                    sellingPrice
            );
            billItemsToSave.add(bi);

            Map<String, Object> itemRes = new HashMap<>();
            itemRes.put("productName", item.getProductName());
            itemRes.put("action", action);
            itemRes.put("productId", finalProduct.getId());
            itemRes.put("costPrice", unitCost);
            itemRes.put("sellingPrice", finalProduct.getPrice());
            itemRes.put("quantity", qty);
            itemResults.add(itemRes);
        }

        SupplierBill bill = new SupplierBill();
        bill.setShopkeeper(shopkeeper);
        bill.setTotalAmount(totalBillAmount);
        bill.setItemCount(request.getLineItems().size());
        bill.setDate(LocalDateTime.now());
        bill.setStatus("processed");
        bill = supplierBillRepository.save(bill);

        for (SupplierBillItem sbi : billItemsToSave) {
            sbi.setSupplierBill(bill);
            supplierBillItemRepository.save(sbi);
        }
        bill.setItems(billItemsToSave);

        try {
            GstPurchaseCreateRequest gstReq = GstPurchaseCreateRequest.builder()
                    .supplierName(request.getSupplierName() != null ? request.getSupplierName() : "General Supplier")
                    .supplierGstin(null)
                    .invoiceNumber(request.getInvoiceNumber() != null ? request.getInvoiceNumber() : "SUP-" + System.currentTimeMillis() % 100000)
                    .invoiceDate(LocalDate.now())
                    .items(gstPurchaseItems)
                    .build();
            gstService.createPurchaseInvoice(shopkeeperId, gstReq);
        } catch (Exception e) {
            log.warn("Failed to record GST purchase invoice: {}", e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("bill", bill);
        response.put("items", itemResults);
        return response;
    }
}
