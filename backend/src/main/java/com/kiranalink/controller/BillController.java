package com.kiranalink.controller;

import com.kiranalink.dto.bill.*;
import com.kiranalink.entity.Bill;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping
    public ResponseEntity<List<Bill>> getBills(@AuthenticationPrincipal UserPrincipal principal) {
        List<Bill> bills = billService.getBills(principal.getId());
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Bill bill = billService.getBillById(id, principal.getId());
        return ResponseEntity.ok(bill);
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BillCreateRequest request) {
        Bill bill = billService.createBill(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }

    @PostMapping("/khata/send-otp")
    public ResponseEntity<Map<String, Object>> sendKhataOtp(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody SendOtpRequest request) {
        Map<String, Object> response = billService.sendKhataOtp(request.getCustomerPhoneNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/khata/verify-otp")
    public ResponseEntity<Bill> verifyKhataOtp(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VerifyOtpRequest request) {
        Bill bill = billService.verifyKhataOtpAndCreateBill(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }

    @PostMapping("/razorpay/create-order")
    public ResponseEntity<RazorpayOrderResponse> createRazorpayOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RazorpayOrderRequest request) {
        RazorpayOrderResponse response = billService.createRazorpayOrder(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/verify-payment")
    public ResponseEntity<Map<String, Object>> verifyRazorpayPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RazorpayVerifyRequest request) {
        Map<String, Object> result = billService.verifyRazorpayPayment(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
