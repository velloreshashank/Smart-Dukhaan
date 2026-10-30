package com.kiranalink.dto.bill;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RazorpayOrderRequest {
    @NotNull(message = "Amount is required")
    private BigDecimal amount; // in paise

    public RazorpayOrderRequest() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
