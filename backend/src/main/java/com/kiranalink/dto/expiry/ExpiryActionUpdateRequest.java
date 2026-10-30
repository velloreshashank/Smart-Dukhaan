package com.kiranalink.dto.expiry;

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
public class ExpiryActionUpdateRequest {
    @NotBlank(message = "Status is required")
    private String status; // RESOLVED, DISCOUNTED, RETURNED_TO_SUPPLIER, WRITTEN_OFF

    private BigDecimal discountApplied;
    private String resolutionNotes;
}
