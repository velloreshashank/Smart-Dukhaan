package com.kiranalink.dto.groupbuy;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyJoinRequest {
    @NotNull(message = "customerId is required")
    private Long customerId;

    @Builder.Default
    private Integer units = 1;
}
