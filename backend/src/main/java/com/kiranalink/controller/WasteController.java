package com.kiranalink.controller;

import com.kiranalink.dto.waste.WasteLogRequest;
import com.kiranalink.entity.WasteLog;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.WasteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/waste")
@RequiredArgsConstructor
public class WasteController {

    private final WasteService wasteService;

    @PostMapping("/log")
    public ResponseEntity<WasteLog> logWaste(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WasteLogRequest request) {
        WasteLog logEntry = wasteService.logWaste(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(logEntry);
    }

    @GetMapping("/history")
    public ResponseEntity<List<WasteLog>> getHistory(@AuthenticationPrincipal UserPrincipal principal) {
        List<WasteLog> list = wasteService.getWasteHistory(principal.getId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> summary = wasteService.getWasteSummary(principal.getId());
        return ResponseEntity.ok(summary);
    }
}
