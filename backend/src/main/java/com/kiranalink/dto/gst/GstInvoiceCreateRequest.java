package com.kiranalink.dto.gst;

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
public class GstInvoiceCreateRequest {
    private String customerId;
    private Long billId;
    private String customerName;
    private String customerGstin;
    private String customerState;

    @NotEmpty(message = "Items array is required")
    private List<RawItemDto> items;
}

