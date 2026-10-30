package com.kiranalink.dto.supplier;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class SupplierLineItemDto {
    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private String unit;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    private BigDecimal customSellingPrice;
    private LocalDate expiryDate;
    private String batchCode;
    private LocalDate mfgDate;
}
