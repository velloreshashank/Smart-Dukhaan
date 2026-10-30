package com.kiranalink.dto.gst;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstClassifyRequest {
    private String name;
    private String productName;
    private String productId;

    public String getProductName() {
        return productName != null ? productName : name;
    }

    public String getName() {
        return name != null ? name : productName;
    }
}

