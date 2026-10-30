package com.kiranalink.controller;

import com.kiranalink.dto.analytics.InsightsResponse;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/insights")
    public ResponseEntity<InsightsResponse> getInsights(@AuthenticationPrincipal UserPrincipal principal) {
        InsightsResponse response = analyticsService.getInsights(principal.getId());
        return ResponseEntity.ok(response);
    }
}
