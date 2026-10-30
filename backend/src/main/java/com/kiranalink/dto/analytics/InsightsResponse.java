package com.kiranalink.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightsResponse {
    private String trends;
    private String productPerformance;
    private String customerBehavior;
    private List<String> recommendations;
}
