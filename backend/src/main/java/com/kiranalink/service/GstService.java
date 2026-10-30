package com.kiranalink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiranalink.dto.gst.*;
import com.kiranalink.entity.GstInvoice;
import com.kiranalink.entity.GstLedger;
import com.kiranalink.entity.GstProduct;
import com.kiranalink.entity.User;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.GstInvoiceRepository;
import com.kiranalink.repository.GstLedgerRepository;
import com.kiranalink.repository.GstProductRepository;
import com.kiranalink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GstService {

    private final GstProductRepository gstProductRepository;
    private final GstInvoiceRepository gstInvoiceRepository;
    private final GstLedgerRepository gstLedgerRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api-key:}")
    private String openAiApiKey;

    public String normalizeName(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public GstClassifyResponse classifyProduct(String productName) {
        String normalized = normalizeName(productName);

        Optional<GstProduct> exactMatch = gstProductRepository.findByNormalizedName(normalized);
        if (exactMatch.isPresent()) {
            GstProduct p = exactMatch.get();
            return GstClassifyResponse.builder()
                    .name(productName)
                    .normalizedName(p.getNormalizedName())
                    .hsnCode(p.getHsnCode())
                    .gstRate(p.getGstRate())
                    .category(p.getCategory())
                    .build();
        }

        String[] words = normalized.split(" ");
        for (String word : words) {
            if (word.length() > 2) {
                List<GstProduct> partialMatches = gstProductRepository.findByNormalizedNameContainingIgnoreCase(word);
                if (!partialMatches.isEmpty()) {
                    GstProduct p = partialMatches.get(0);
                    return GstClassifyResponse.builder()
                            .name(productName)
                            .normalizedName(p.getNormalizedName())
                            .hsnCode(p.getHsnCode())
                            .gstRate(p.getGstRate())
                            .category(p.getCategory())
                            .build();
                }
            }
        }

        GstClassifyResponse aiResult = null;
        if (openAiApiKey != null && !openAiApiKey.isBlank() && !"dummy_key_for_build".equals(openAiApiKey)) {
            aiResult = classifyViaOpenAi(productName, normalized);
        }

        if (aiResult == null) {
            aiResult = classifyHeuristically(productName, normalized);
        }

        try {
            GstProduct newGst = new GstProduct();
            newGst.setNormalizedName(aiResult.getNormalizedName());
            newGst.setHsnCode(aiResult.getHsnCode());
            newGst.setGstRate(aiResult.getGstRate());
            newGst.setCategory(aiResult.getCategory());
            gstProductRepository.save(newGst);
        } catch (Exception e) {
            log.warn("Could not cache GST product {}: {}", productName, e.getMessage());
        }

        return aiResult;
    }

    private GstClassifyResponse classifyHeuristically(String productName, String normalized) {
        if (normalized.contains("rice") || normalized.contains("wheat") || normalized.contains("atta")
                || normalized.contains("dal") || normalized.contains("milk") || normalized.contains("potato")
                || normalized.contains("onion") || normalized.contains("tomato")) {
            return GstClassifyResponse.builder()
                    .name(productName)
                    .normalizedName(normalized)
                    .hsnCode("1006")
                    .gstRate(BigDecimal.ZERO)
                    .category("Grains & Staples")
                    .build();
        } else if (normalized.contains("oil") || normalized.contains("sugar") || normalized.contains("tea")
                || normalized.contains("coffee") || normalized.contains("paneer") || normalized.contains("ghee")
                || normalized.contains("spice") || normalized.contains("masala")) {
            return GstClassifyResponse.builder()
                    .name(productName)
                    .normalizedName(normalized)
                    .hsnCode("1512")
                    .gstRate(BigDecimal.valueOf(5))
                    .category("Packaged Grocery")
                    .build();
        } else if (normalized.contains("soap") || normalized.contains("detergent") || normalized.contains("shampoo")
                || normalized.contains("paste") || normalized.contains("clean")) {
            return GstClassifyResponse.builder()
                    .name(productName)
                    .normalizedName(normalized)
                    .hsnCode("3402")
                    .gstRate(BigDecimal.valueOf(18))
                    .category("Cleaning & Personal Care")
                    .build();
        } else if (normalized.contains("drink") || normalized.contains("coke") || normalized.contains("pepsi")
                || normalized.contains("soda")) {
            return GstClassifyResponse.builder()
                    .name(productName)
                    .normalizedName(normalized)
                    .hsnCode("2202")
                    .gstRate(BigDecimal.valueOf(28))
                    .category("Beverages")
                    .build();
        }

        return GstClassifyResponse.builder()
                .name(productName)
                .normalizedName(normalized)
                .hsnCode("2106")
                .gstRate(BigDecimal.valueOf(5))
                .category("General Grocery")
                .build();
    }

    private GstClassifyResponse classifyViaOpenAi(String productName, String normalized) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are an Indian GST classification specialist. Return ONLY a JSON object with: name, normalizedName, hsnCode, gstRate (number 0, 5, 12, 18, 28), and category."),
                            Map.of("role", "user", "content", "Classify grocery item: " + productName)
                    ),
                    "response_format", Map.of("type", "json_object")
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", entity, String.class);
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode parsed = objectMapper.readTree(content);

            return GstClassifyResponse.builder()
                    .name(productName)
                    .normalizedName(parsed.path("normalizedName").asText(normalized))
                    .hsnCode(parsed.path("hsnCode").asText("2106"))
                    .gstRate(BigDecimal.valueOf(parsed.path("gstRate").asDouble(5.0)))
                    .category(parsed.path("category").asText("General Grocery"))
                    .build();
        } catch (Exception e) {
            log.warn("OpenAI GST classification failed for {}: {}", productName, e.getMessage());
            return null;
        }
    }

    public GstInvoiceCalculation calculateInvoice(List<RawItemDto> rawItems) {
        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        List<GstInvoiceCalculation.CalculatedItem> calculatedItems = new ArrayList<>();

        for (RawItemDto item : rawItems) {
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
            BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal rate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
            boolean inclusive = Boolean.TRUE.equals(item.getPriceIncludesGST());

            BigDecimal lineEnteredTotal = price.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal baseAmount;
            BigDecimal gstAmount;

            if (inclusive && rate.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal divisor = BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                baseAmount = lineEnteredTotal.divide(divisor, 2, RoundingMode.HALF_UP);
                gstAmount = lineEnteredTotal.subtract(baseAmount);
            } else {
                baseAmount = lineEnteredTotal;
                gstAmount = baseAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            BigDecimal cgst = gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal sgst = gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = inclusive ? lineEnteredTotal : baseAmount.add(gstAmount);

            totalBase = totalBase.add(baseAmount);
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            grandTotal = grandTotal.add(lineTotal);

            calculatedItems.add(GstInvoiceCalculation.CalculatedItem.builder()
                    .productId(item.getProductId())
                    .name(item.getName())
                    .hsnCode(item.getHsnCode())
                    .gstRate(rate)
                    .quantity(qty)
                    .unitPrice(price)
                    .priceIncludesGst(inclusive)
                    .baseAmount(baseAmount)
                    .cgstAmount(cgst)
                    .sgstAmount(sgst)
                    .totalAmount(lineTotal)
                    .build());
        }

        BigDecimal totalGst = totalCgst.add(totalSgst);

        return GstInvoiceCalculation.builder()
                .items(calculatedItems)
                .totalBaseAmount(totalBase)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalGst(totalGst)
                .grandTotal(grandTotal)
                .build();
    }

    @Transactional
    public GstInvoice createSalesInvoice(Long shopkeeperId, GstInvoiceCreateRequest request) {
        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        GstInvoiceCalculation calc = calculateInvoice(request.getItems());
        LocalDate now = LocalDate.now();

        GstInvoice invoice = new GstInvoice();
        invoice.setShopkeeper(shopkeeper);
        invoice.setBillId(request.getBillId());
        invoice.setInvoiceType("sale");
        invoice.setMonth(now.getMonthValue());
        invoice.setYear(now.getYear());
        invoice.setTotalBaseAmount(calc.getTotalBaseAmount());
        invoice.setTotalCGST(calc.getTotalCgst());
        invoice.setTotalSGST(calc.getTotalSgst());
        invoice.setTotalGST(calc.getTotalGst());
        invoice.setGrandTotal(calc.getGrandTotal());

        invoice = gstInvoiceRepository.save(invoice);

        // Record GST Ledger Output entry
        GstLedger ledger = new GstLedger();
        ledger.setShopkeeper(shopkeeper);
        ledger.setType("output");
        ledger.setGstInvoice(invoice);
        ledger.setReferenceId(request.getBillId());
        ledger.setReferenceType("Bill");
        ledger.setTotalBaseAmount(calc.getTotalBaseAmount());
        ledger.setTotalCGST(calc.getTotalCgst());
        ledger.setTotalSGST(calc.getTotalSgst());
        ledger.setTotalGST(calc.getTotalGst());
        ledger.setGrandTotal(calc.getGrandTotal());
        ledger.setMonth(now.getMonthValue());
        ledger.setYear(now.getYear());
        gstLedgerRepository.save(ledger);

        return invoice;
    }

    @Transactional
    public GstInvoice createPurchaseInvoice(Long shopkeeperId, GstPurchaseCreateRequest request) {
        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        GstInvoiceCalculation calc = calculateInvoice(request.getItems());
        LocalDate date = request.getInvoiceDate() != null ? request.getInvoiceDate() : LocalDate.now();

        GstInvoice invoice = new GstInvoice();
        invoice.setShopkeeper(shopkeeper);
        invoice.setInvoiceType("purchase");
        invoice.setMonth(date.getMonthValue());
        invoice.setYear(date.getYear());
        invoice.setTotalBaseAmount(calc.getTotalBaseAmount());
        invoice.setTotalCGST(calc.getTotalCgst());
        invoice.setTotalSGST(calc.getTotalSgst());
        invoice.setTotalGST(calc.getTotalGst());
        invoice.setGrandTotal(calc.getGrandTotal());

        invoice = gstInvoiceRepository.save(invoice);

        // Record GST Ledger Input (ITC) entry
        GstLedger ledger = new GstLedger();
        ledger.setShopkeeper(shopkeeper);
        ledger.setType("input");
        ledger.setGstInvoice(invoice);
        ledger.setReferenceType("SupplierBill");
        ledger.setTotalBaseAmount(calc.getTotalBaseAmount());
        ledger.setTotalCGST(calc.getTotalCgst());
        ledger.setTotalSGST(calc.getTotalSgst());
        ledger.setTotalGST(calc.getTotalGst());
        ledger.setGrandTotal(calc.getGrandTotal());
        ledger.setMonth(date.getMonthValue());
        ledger.setYear(date.getYear());
        gstLedgerRepository.save(ledger);

        return invoice;
    }

    public GstSummaryResponse getMonthlySummary(Long shopkeeperId, String monthStr) {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        if (monthStr != null && monthStr.contains("-")) {
            String[] parts = monthStr.split("-");
            try {
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
            } catch (Exception ignored) {}
        }

        List<GstLedger> salesLedgers = gstLedgerRepository.findByShopkeeperIdAndTypeAndMonthAndYearOrderByCreatedAtDesc(shopkeeperId, "output", month, year);
        List<GstLedger> purchaseLedgers = gstLedgerRepository.findByShopkeeperIdAndTypeAndMonthAndYearOrderByCreatedAtDesc(shopkeeperId, "input", month, year);

        BigDecimal salesTurnover = BigDecimal.ZERO;
        BigDecimal outCgst = BigDecimal.ZERO;
        BigDecimal outSgst = BigDecimal.ZERO;
        for (GstLedger l : salesLedgers) {
            salesTurnover = salesTurnover.add(l.getTotalBaseAmount() != null ? l.getTotalBaseAmount() : BigDecimal.ZERO);
            outCgst = outCgst.add(l.getTotalCGST() != null ? l.getTotalCGST() : BigDecimal.ZERO);
            outSgst = outSgst.add(l.getTotalSGST() != null ? l.getTotalSGST() : BigDecimal.ZERO);
        }

        BigDecimal purchaseTurnover = BigDecimal.ZERO;
        BigDecimal inCgst = BigDecimal.ZERO;
        BigDecimal inSgst = BigDecimal.ZERO;
        for (GstLedger l : purchaseLedgers) {
            purchaseTurnover = purchaseTurnover.add(l.getTotalBaseAmount() != null ? l.getTotalBaseAmount() : BigDecimal.ZERO);
            inCgst = inCgst.add(l.getTotalCGST() != null ? l.getTotalCGST() : BigDecimal.ZERO);
            inSgst = inSgst.add(l.getTotalSGST() != null ? l.getTotalSGST() : BigDecimal.ZERO);
        }

        BigDecimal outTotal = outCgst.add(outSgst);
        BigDecimal inTotal = inCgst.add(inSgst);
        BigDecimal netPayable = outTotal.subtract(inTotal).max(BigDecimal.ZERO);

        return GstSummaryResponse.builder()
                .month(month)
                .year(year)
                .totalSales(salesTurnover)
                .totalOutputGST(outTotal)
                .totalInputGST(inTotal)
                .netGSTPayable(netPayable)
                .outputCGST(outCgst)
                .outputSGST(outSgst)
                .inputCGST(inCgst)
                .inputSGST(inSgst)
                .build();
    }

    public ItrSummaryResponse getItrSummary(Long shopkeeperId, String financialYear) {
        List<GstLedger> ledgers = gstLedgerRepository.findByShopkeeperIdOrderByCreatedAtDesc(shopkeeperId);

        BigDecimal grossTurnover = BigDecimal.ZERO;
        BigDecimal totalPurchases = BigDecimal.ZERO;
        Map<String, BigDecimal[]> monthlyMap = new HashMap<>();

        for (GstLedger l : ledgers) {
            String mKey = String.format("%04d-%02d", l.getYear(), l.getMonth());
            monthlyMap.putIfAbsent(mKey, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] pair = monthlyMap.get(mKey);

            if ("output".equalsIgnoreCase(l.getType())) {
                grossTurnover = grossTurnover.add(l.getTotalBaseAmount() != null ? l.getTotalBaseAmount() : BigDecimal.ZERO);
                pair[0] = pair[0].add(l.getTotalBaseAmount() != null ? l.getTotalBaseAmount() : BigDecimal.ZERO);
            } else {
                totalPurchases = totalPurchases.add(l.getTotalBaseAmount() != null ? l.getTotalBaseAmount() : BigDecimal.ZERO);
                pair[1] = pair[1].add(l.getTotalBaseAmount() != null ? l.getTotalBaseAmount() : BigDecimal.ZERO);
            }
        }

        List<ItrSummaryResponse.MonthlyFinancialBreakdown> monthlyList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> e : monthlyMap.entrySet()) {
            BigDecimal turn = e.getValue()[0];
            BigDecimal pur = e.getValue()[1];
            monthlyList.add(ItrSummaryResponse.MonthlyFinancialBreakdown.builder()
                    .month(e.getKey())
                    .turnover(turn)
                    .purchases(pur)
                    .grossMargin(turn.subtract(pur))
                    .build());
        }

        BigDecimal grossProfit = grossTurnover.subtract(totalPurchases);
        BigDecimal estimatedNetIncome = grossTurnover.multiply(BigDecimal.valueOf(0.06)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedTax44AD = estimatedNetIncome.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);

        return ItrSummaryResponse.builder()
                .financialYear(financialYear != null ? financialYear : "2024-25")
                .grossTurnover(grossTurnover)
                .totalPurchases(totalPurchases)
                .grossProfit(grossProfit)
                .estimatedNetIncome(estimatedNetIncome)
                .estimatedTax44AD(estimatedTax44AD)
                .presumptiveSchemeRecommendation("Section 44AD of the Income Tax Act allows Kirana stores with turnover under ₹2 Crore (or ₹3 Crore if digital > 95%) to declare 6% of digital turnover or 8% of cash turnover as presumptive net profit without maintaining bulky formal books of accounts.")
                .monthlyBreakdown(monthlyList)
                .build();
    }
}
