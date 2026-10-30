package com.kiranalink.dto.whatsapp;

import jakarta.validation.constraints.NotEmpty;
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
public class OrderItemsUpdateRequest {
    @NotEmpty(message = "Items list cannot be empty")
    private List<OrderItemUpdateDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemUpdateDto {
        private Long productId;
        private String name;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
    }
}
