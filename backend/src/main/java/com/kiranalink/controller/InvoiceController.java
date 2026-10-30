package com.kiranalink.controller;

import com.kiranalink.entity.Invoice;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.InvoiceRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRecoveryService invoiceRecoveryService;

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> list = invoiceRecoveryService.getAllInvoices();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceRecoveryService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        Invoice created = invoiceRecoveryService.createInvoice(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/import-khata")
    public ResponseEntity<Map<String, Object>> importKhata(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> response = invoiceRecoveryService.importKhata(principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/remind")
    public ResponseEntity<Invoice> triggerReminder(
            @PathVariable Long id,
            @RequestParam(required = false) String channel) {
        Invoice invoice = invoiceRecoveryService.triggerReminder(id, channel);
        return ResponseEntity.ok(invoice);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Invoice> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        String reason = (String) body.get("reason");
        Invoice invoice = invoiceRecoveryService.updateStatus(id, status, null, reason);
        return ResponseEntity.ok(invoice);
    }
}
