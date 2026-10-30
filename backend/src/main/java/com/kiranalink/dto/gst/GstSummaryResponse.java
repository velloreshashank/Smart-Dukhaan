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
public class GstSummaryResponse {
    private Integer month;
    private Integer year;
    private BigDecimal totalSales;
    private BigDecimal totalOutputGST;
    private BigDecimal totalInputGST;
    private BigDecimal netGSTPayable;
    private BigDecimal outputCGST;
    private BigDecimal outputSGST;
    private BigDecimal inputCGST;
    private BigDecimal inputSGST;
}

