package com.kiranalink.dto.expiry;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    private String batchNumber;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private LocalDate manufactureDate;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private BigDecimal purchasePrice;
    private BigDecimal mrp;
    private String supplierName;
}
