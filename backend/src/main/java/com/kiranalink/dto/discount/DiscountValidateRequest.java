package com.kiranalink.dto.discount;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountValidateRequest {
    @NotBlank(message = "Code is required")
    private String code;

    private Long customerId;
    private BigDecimal billAmount;
}
