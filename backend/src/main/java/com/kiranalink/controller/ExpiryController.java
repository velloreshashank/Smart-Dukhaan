package com.kiranalink.controller;

import com.kiranalink.dto.expiry.BatchCreateRequest;
import com.kiranalink.dto.expiry.ExpiryActionUpdateRequest;
import com.kiranalink.entity.ExpiryAction;
import com.kiranalink.entity.InventoryBatch;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.ExpiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expiry")
@RequiredArgsConstructor
public class ExpiryController {

    private final ExpiryService expiryService;

    @PostMapping("/batches")
    public ResponseEntity<InventoryBatch> createBatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BatchCreateRequest request) {
        InventoryBatch batch = expiryService.createBatch(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(batch);
    }

    @GetMapping("/batches")
    public ResponseEntity<List<InventoryBatch>> getBatches(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long productId) {
        List<InventoryBatch> batches = expiryService.getBatches(principal.getId(), status, productId);
        return ResponseEntity.ok(batches);
    }

    @PatchMapping("/batches/{id}")
    public ResponseEntity<InventoryBatch> updateBatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        String batchCode = (String) body.get("batchCode");
        InventoryBatch batch = expiryService.updateBatch(principal.getId(), id, status, batchCode, null, null);
        return ResponseEntity.ok(batch);
    }

    @PostMapping("/recompute")
    public ResponseEntity<Map<String, Object>> recomputeQueue(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> result = expiryService.recomputeQueue(principal.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/queue")
    public ResponseEntity<Map<String, Object>> getQueue(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) String status) {
        Map<String, Object> response = expiryService.getQueue(principal.getId(), bucket, status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/queue/{id}")
    public ResponseEntity<ExpiryAction> updateAction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ExpiryActionUpdateRequest request) {
        ExpiryAction action = expiryService.updateAction(principal.getId(), id, request);
        return ResponseEntity.ok(action);
    }
}
