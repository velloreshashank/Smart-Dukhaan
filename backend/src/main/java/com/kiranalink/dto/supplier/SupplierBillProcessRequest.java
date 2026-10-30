package com.kiranalink.dto.supplier;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierBillProcessRequest {
    private String supplierName;
    private String invoiceNumber;

    @NotEmpty(message = "Line items cannot be empty")
    @Valid
    private List<SupplierLineItemDto> lineItems;
}
