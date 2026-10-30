package com.kiranalink.controller;

import com.kiranalink.dto.whatsapp.OrderConvertToBillRequest;
import com.kiranalink.dto.whatsapp.OrderItemsUpdateRequest;
import com.kiranalink.dto.whatsapp.OrderStatusRequest;
import com.kiranalink.entity.WhatsAppOrder;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.WhatsAppOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppOrderService whatsAppOrderService;

    @GetMapping("/orders")
    public ResponseEntity<List<WhatsAppOrder>> getOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String status) {
        List<WhatsAppOrder> orders = whatsAppOrderService.getOrders(principal.getId(), status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<WhatsAppOrder> getOrderById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        WhatsAppOrder order = whatsAppOrderService.getOrderById(principal.getId(), id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<WhatsAppOrder> updateOrderStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {
        WhatsAppOrder order = whatsAppOrderService.updateStatus(principal.getId(), id, request);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/orders/{id}/items")
    public ResponseEntity<WhatsAppOrder> updateOrderItems(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody OrderItemsUpdateRequest request) {
        WhatsAppOrder order = whatsAppOrderService.updateOrderItems(principal.getId(), id, request);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/orders/{id}/convert-to-bill")
    public ResponseEntity<Map<String, Object>> convertToBill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) OrderConvertToBillRequest request) {
        Map<String, Object> result = whatsAppOrderService.convertToBill(principal.getId(), id, request);
        return ResponseEntity.ok(result);
    }
}
