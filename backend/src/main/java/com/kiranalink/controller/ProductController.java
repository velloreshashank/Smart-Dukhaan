package com.kiranalink.controller;

import com.kiranalink.dto.product.ProductRequest;
import com.kiranalink.entity.Product;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock) {
        List<Product> products = productService.getProducts(principal.getId(), search, category, lowStock);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Product product = productService.getProductById(principal.getId(), id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        Product updated = productService.updateProduct(principal.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> replaceProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        Product updated = productService.updateProduct(principal.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        productService.deleteProduct(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedStarterProducts(@AuthenticationPrincipal UserPrincipal principal) {
        int count = productService.seedStarterProducts(principal.getId());
        return ResponseEntity.ok(Map.of("message", "Starter products seeded successfully", "count", count));
    }
}
