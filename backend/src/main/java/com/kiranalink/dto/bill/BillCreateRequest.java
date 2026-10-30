package com.kiranalink.dto.bill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCreateRequest {
    private Long customerId;

    private String customerPhoneNumber;

    private String customerName;

    @NotEmpty(message = "Items cannot be empty")
    private List<BillItemDto> items;

    @NotBlank(message = "Payment type is required")
    private String paymentType; // 'cash', 'online', 'ledger'

    private BigDecimal discountAmount;
    private String discountCode;
}

