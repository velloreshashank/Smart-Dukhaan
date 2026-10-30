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
public class GstClassifyResponse {
    private String name;
    private String normalizedName;
    private String hsnCode;
    private BigDecimal gstRate;
    private String category;
}
