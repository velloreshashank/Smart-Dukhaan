package com.kiranalink.dto.gst;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawItemDto {
    private String productId;
    private String name;
    private String hsnCode;
    private BigDecimal gstRate;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    @Builder.Default
    private Boolean priceIncludesGST = true;
}

