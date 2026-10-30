package com.kiranalink.dto.discount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCreateRequest {
    private Long productId;
    private String description;

    @NotBlank(message = "discountType is required")
    private String discountType; // percentage, flat

    @NotNull(message = "discountValue is required")
    @DecimalMin(value = "0.01", message = "discountValue must be greater than zero")
    private BigDecimal discountValue;

    private BigDecimal minPurchase;
    private Integer maxUses;

    @NotNull(message = "validUntil is required")
    private LocalDateTime validUntil;

    private String createdFor; // manual, expiry_flash, bulk, loyalty
    private Long linkedBatchId;
}
