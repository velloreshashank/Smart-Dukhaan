package com.kiranalink.dto.groupbuy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Long shopkeeperId;
    private String hostShopName;

    @NotNull(message = "Target units is required")
    private Integer targetUnits;

    @NotNull(message = "Original price is required")
    private BigDecimal originalPrice;

    @NotNull(message = "Group price is required")
    private BigDecimal groupPrice;

    private LocalDateTime deadline;
    private Double latitude;
    private Double longitude;

    private List<GroupBuyProductItemDto> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupBuyProductItemDto {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
