package com.kiranalink.controller;

import com.kiranalink.dto.supplier.SupplierBillProcessRequest;
import com.kiranalink.entity.SupplierBill;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.SupplierBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supplier-bills")
@RequiredArgsConstructor
public class SupplierBillController {

    private final SupplierBillService supplierBillService;

    @GetMapping
    public ResponseEntity<List<SupplierBill>> getBills(@AuthenticationPrincipal UserPrincipal principal) {
        List<SupplierBill> bills = supplierBillService.getBills(principal.getId());
        return ResponseEntity.ok(bills);
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processBill(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SupplierBillProcessRequest request) {
        Map<String, Object> result = supplierBillService.processSupplierBill(principal.getId(), request);
        return ResponseEntity.ok(result);
    }
}
