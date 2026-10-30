package com.kiranalink.controller;

import com.kiranalink.dto.ledger.LedgerPaymentRequest;
import com.kiranalink.entity.LedgerEntry;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LedgerEntry>> getLedgerByCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long customerId) {
        List<LedgerEntry> entries = ledgerService.getEntriesByCustomer(principal.getId(), customerId);
        return ResponseEntity.ok(entries);
    }

    @PostMapping("/payment")
    public ResponseEntity<LedgerEntry> recordPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody LedgerPaymentRequest request) {
        LedgerEntry entry = ledgerService.recordPayment(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }
}
