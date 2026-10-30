package com.kiranalink.controller;

import com.kiranalink.dto.gst.*;
import com.kiranalink.entity.GstInvoice;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.GstService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GstController {

    private final GstService gstService;

    @PostMapping("/api/gst/classify")
    public ResponseEntity<GstClassifyResponse> classifyProduct(@Valid @RequestBody GstClassifyRequest request) {
        GstClassifyResponse response = gstService.classifyProduct(request.getProductName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/gst/calculate")
    public ResponseEntity<GstInvoiceCalculation> calculateInvoice(@RequestBody List<RawItemDto> items) {
        GstInvoiceCalculation response = gstService.calculateInvoice(items);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/gst/invoices/sales")
    public ResponseEntity<GstInvoice> createSalesInvoice(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GstInvoiceCreateRequest request) {
        GstInvoice invoice = gstService.createSalesInvoice(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @PostMapping("/api/gst/invoices/purchase")
    public ResponseEntity<GstInvoice> createPurchaseInvoice(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GstPurchaseCreateRequest request) {
        GstInvoice invoice = gstService.createPurchaseInvoice(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping({"/api/gst/summary", "/api/reports/gst/summary"})
    public ResponseEntity<GstSummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String month) {
        String targetMonth = (month != null && !month.isBlank()) ? month :
                String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        GstSummaryResponse response = gstService.getMonthlySummary(principal.getId(), targetMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/api/gst/itr-summary", "/api/reports/itr/summary"})
    public ResponseEntity<ItrSummaryResponse> getItrSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String financialYear) {
        ItrSummaryResponse response = gstService.getItrSummary(principal.getId(), financialYear);
        return ResponseEntity.ok(response);
    }
}
