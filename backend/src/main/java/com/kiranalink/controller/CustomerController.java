package com.kiranalink.controller;

import com.kiranalink.dto.customer.CustomerRequest;
import com.kiranalink.entity.Customer;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Customer>> getCustomers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search) {
        List<Customer> customers = customerService.getCustomers(principal.getId(), search);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id, principal.getId());
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Customer> getCustomerByPhone(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String phone) {
        Customer customer = customerService.getCustomerByPhone(phone, principal.getId());
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CustomerRequest request) {
        Customer customer = customerService.createCustomer(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody CustomerRequest request) {
        Customer updated = customerService.updateCustomer(id, principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> replaceCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody CustomerRequest request) {
        Customer updated = customerService.updateCustomer(id, principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        customerService.deleteCustomer(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/recalculate-score")
    public ResponseEntity<Customer> recalculateScore(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Customer customer = customerService.recalculateKhata(id, principal.getId());
        return ResponseEntity.ok(customer);
    }
}
