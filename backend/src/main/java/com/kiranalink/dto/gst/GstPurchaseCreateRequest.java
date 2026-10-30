package com.kiranalink.dto.gst;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstPurchaseCreateRequest {
    private String supplierBillId;
    private String supplierName;
    private String supplierGstin;
    private String invoiceNumber;
    private LocalDate invoiceDate;

    @NotEmpty(message = "Items array is required")
    private List<RawItemDto> items;
}

