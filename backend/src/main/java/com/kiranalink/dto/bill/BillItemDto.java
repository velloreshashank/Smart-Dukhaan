package com.kiranalink.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemDto {
    private String productId;
    private String name;
    private BigDecimal quantity;
    private BigDecimal price;
}

