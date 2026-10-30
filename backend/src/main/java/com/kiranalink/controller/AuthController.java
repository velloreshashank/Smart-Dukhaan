package com.kiranalink.controller;

import com.kiranalink.dto.auth.*;
import com.kiranalink.security.UserPrincipal;
import com.kiranalink.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        AuthResponse.UserDto user = authService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/profile")
    public ResponseEntity<AuthResponse.UserDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ProfileUpdateRequest request) {
        AuthResponse.UserDto updated = authService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) DeleteAccountRequest request) {
        authService.deleteAccount(principal.getId(), request != null ? request : new DeleteAccountRequest());
        return ResponseEntity.noContent().build();
    }
}
