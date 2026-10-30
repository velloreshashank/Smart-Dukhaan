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
public class GstInvoiceCalculation {
    private List<CalculatedItem> items;
    private BigDecimal totalBaseAmount;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalGst;
    private BigDecimal grandTotal;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculatedItem {
        private String productId;
        private String name;
        private String hsnCode;
        private BigDecimal gstRate;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private Boolean priceIncludesGst;
        private BigDecimal baseAmount;
        private BigDecimal cgstAmount;
        private BigDecimal sgstAmount;
        private BigDecimal totalAmount;
    }
}
