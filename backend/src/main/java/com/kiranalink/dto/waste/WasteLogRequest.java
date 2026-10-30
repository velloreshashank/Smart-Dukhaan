package com.kiranalink.dto.waste;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteLogRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long batchId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotBlank(message = "Reason is required")
    private String reason; // EXPIRED, DAMAGED, SPOILED, THEFT, OTHER

    private BigDecimal estimatedLossAmount;
    private String notes;
}
