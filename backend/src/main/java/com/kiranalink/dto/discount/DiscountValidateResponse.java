package com.kiranalink.dto.discount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountValidateResponse {
    private boolean valid;
    private String message;
    private DiscountInfo discount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountInfo {
        private String code;
        private String description;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal discountAmount;
    }
}
