package com.kiranalink.dto.whatsapp;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusRequest {
    @NotBlank(message = "Status is required")
    private String status; // received, confirmed, packed, ready, delivered, cancelled
}
