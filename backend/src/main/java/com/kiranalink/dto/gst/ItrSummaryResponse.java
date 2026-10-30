package com.kiranalink.dto.gst;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItrSummaryResponse {
    private String financialYear;
    private BigDecimal grossTurnover;
    private BigDecimal totalPurchases;
    private BigDecimal grossProfit;
    private BigDecimal estimatedNetIncome;
    private BigDecimal estimatedTax44AD;
    private String presumptiveSchemeRecommendation;
    private List<MonthlyFinancialBreakdown> monthlyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyFinancialBreakdown {
        private String month;
        private BigDecimal turnover;
        private BigDecimal purchases;
        private BigDecimal grossMargin;
    }
}
