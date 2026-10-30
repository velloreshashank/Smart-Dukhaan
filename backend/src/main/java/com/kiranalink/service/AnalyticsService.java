package com.kiranalink.service;

import com.kiranalink.dto.analytics.InsightsResponse;
import com.kiranalink.entity.Bill;
import com.kiranalink.entity.BillItem;
import com.kiranalink.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final BillRepository billRepository;

    public InsightsResponse getInsights(Long shopkeeperId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Bill> bills = billRepository.findByShopkeeperIdAndCreatedAtAfter(shopkeeperId, thirtyDaysAgo);

        if (bills.isEmpty()) {
            return InsightsResponse.builder()
                    .trends("No sales data recorded in the last 30 days.")
                    .productPerformance("Start billing to view top-selling groceries.")
                    .customerBehavior("No customer transaction history yet.")
                    .recommendations(List.of(
                            "Record your first sale to activate Smart Dukaan AI insights!",
                            "Set up customer Khata limits to safely offer store credit."
                    ))
                    .build();
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, BigDecimal> productRevenueMap = new HashMap<>();
        Map<String, BigDecimal> productQtyMap = new HashMap<>();
        Map<String, Integer> paymentMethodsMap = new HashMap<>();

        for (Bill bill : bills) {
            BigDecimal amt = bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO;
            totalRevenue = totalRevenue.add(amt);

            String mode = bill.getPaymentType() != null ? bill.getPaymentType().toLowerCase() : "cash";
            paymentMethodsMap.put(mode, paymentMethodsMap.getOrDefault(mode, 0) + 1);

            if (bill.getItems() != null) {
                for (BillItem item : bill.getItems()) {
                    String name = item.getName() != null ? item.getName() : "Item";
                    BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
                    BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    BigDecimal lineTotal = price.multiply(qty);

                    productRevenueMap.put(name, productRevenueMap.getOrDefault(name, BigDecimal.ZERO).add(lineTotal));
                    productQtyMap.put(name, productQtyMap.getOrDefault(name, BigDecimal.ZERO).add(qty));
                }
            }
        }

        BigDecimal avgTicket = totalRevenue.divide(BigDecimal.valueOf(bills.size()), 2, RoundingMode.HALF_UP);

        // Sort top products
        List<Map.Entry<String, BigDecimal>> topProducts = new ArrayList<>(productRevenueMap.entrySet());
        topProducts.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        String topProductName = !topProducts.isEmpty() ? topProducts.get(0).getKey() : "Grocery items";

        int cashCount = paymentMethodsMap.getOrDefault("cash", 0);
        int onlineCount = paymentMethodsMap.getOrDefault("online", 0);
        int ledgerCount = paymentMethodsMap.getOrDefault("ledger", 0);

        String trends = String.format("Generated ₹%s across %d transactions over the last 30 days (Average ticket size: ₹%s).",
                totalRevenue.setScale(2, RoundingMode.HALF_UP), bills.size(), avgTicket);

        String productPerf = String.format("'%s' is your highest grossing item contributing ₹%s in sales.",
                topProductName, !topProducts.isEmpty() ? topProducts.get(0).getValue().setScale(2, RoundingMode.HALF_UP) : "0");

        String customerBehavior = String.format("Payment breakdown: Cash: %d, Online (UPI/Cards): %d, Khata (Store Credit): %d. %s",
                cashCount, onlineCount, ledgerCount,
                ledgerCount > 0 ? "Khata credit customers drive repeat visits." : "Prompt customers to build a Khata credit score.");

        List<String> recommendations = List.of(
                "Maintain healthy stock levels for '" + topProductName + "' to avoid stock-outs during peak evening hours.",
                "Review expiring batches in the Expiry Action Queue to bundle items before shelf expiration.",
                "Ensure timely repayments by scheduling automated WhatsApp reminders for customers with Khata balances.",
                "Consider joining community Group-Buy deals on staples like Basmati Rice and Edible Oil to increase margins by 6-10%."
        );

        return InsightsResponse.builder()
                .trends(trends)
                .productPerformance(productPerf)
                .customerBehavior(customerBehavior)
                .recommendations(recommendations)
                .build();
    }
}
