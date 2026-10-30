package com.kiranalink.controller;

import com.kiranalink.dto.discount.*;
import com.kiranalink.entity.DiscountCode;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    public ResponseEntity<DiscountCode> createDiscount(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DiscountCreateRequest request) {
        DiscountCode discount = discountService.createDiscount(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(discount);
    }

    @GetMapping
    public ResponseEntity<List<DiscountCode>> getDiscounts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String createdFor) {
        List<DiscountCode> list = discountService.getDiscounts(principal.getId(), isActive, createdFor);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DiscountCode> updateDiscount(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Boolean isActive = body.containsKey("isActive") ? (Boolean) body.get("isActive") : null;
        BigDecimal discountValue = body.containsKey("discountValue") ? new BigDecimal(body.get("discountValue").toString()) : null;
        Integer maxUses = body.containsKey("maxUses") ? Integer.valueOf(body.get("maxUses").toString()) : null;

        DiscountCode updated = discountService.updateDiscount(principal.getId(), id, isActive, discountValue, maxUses, null);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/validate")
    public ResponseEntity<DiscountValidateResponse> validateDiscount(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DiscountValidateRequest request) {
        DiscountValidateResponse response = discountService.validateDiscount(principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<DiscountCode> applyDiscount(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) DiscountApplyRequest request) {
        DiscountCode applied = discountService.applyDiscount(
                principal.getId(),
                id,
                request != null ? request : new DiscountApplyRequest()
        );
        return ResponseEntity.ok(applied);
    }

    @PostMapping("/{id}/send/{customerId}")
    public ResponseEntity<Map<String, String>> sendToCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long customerId) {
        discountService.sendDiscountToCustomer(principal.getId(), id, customerId);
        return ResponseEntity.ok(Map.of("message", "Discount code sent successfully via WhatsApp"));
    }
}
