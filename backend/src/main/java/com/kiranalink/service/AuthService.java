package com.kiranalink.service;

import com.kiranalink.dto.auth.*;
import com.kiranalink.entity.User;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.UserRepository;
import com.kiranalink.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProductService productService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }
        if (request.getPhone() != null && userRepository.existsByPhoneNumber(request.getPhone())) {
            throw new BadRequestException("An account with this phone number already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setShopName(request.getShopName() != null ? request.getShopName() : request.getName() + "'s Kirana");
        user.setAddress(request.getAddress());
        if (request.getLatitude() != null) user.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        if (request.getLongitude() != null) user.setLongitude(BigDecimal.valueOf(request.getLongitude()));

        user = userRepository.save(user);

        try {
            productService.seedStarterProducts(user.getId());
        } catch (Exception e) {
            log.warn("Could not seed starter products for user {}: {}", user.getId(), e.getMessage());
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDto(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmail();
        if (identifier == null || identifier.isBlank()) {
            identifier = request.getPhone();
        }

        if (identifier == null || identifier.isBlank()) {
            throw new BadRequestException("Email or phone number is required");
        }

        final String lookupKey = identifier;
        User user = userRepository.findByEmail(lookupKey)
                .or(() -> userRepository.findByPhoneNumber(lookupKey))
                .orElseThrow(() -> new BadRequestException("Invalid email/phone or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email/phone or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDto(user))
                .build();
    }

    public AuthResponse.UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToUserDto(user);
    }

    @Transactional
    public AuthResponse.UserDto updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getShopName() != null) user.setShopName(request.getShopName());
        if (request.getPhone() != null) user.setPhoneNumber(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getLatitude() != null) user.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        if (request.getLongitude() != null) user.setLongitude(BigDecimal.valueOf(request.getLongitude()));

        user = userRepository.save(user);
        return mapToUserDto(user);
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getPassword() != null && !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Password does not match. Account deletion rejected.");
        }

        userRepository.delete(user);
    }

    private AuthResponse.UserDto mapToUserDto(User user) {
        Double lat = user.getLatitude() != null ? user.getLatitude().doubleValue() : null;
        Double lon = user.getLongitude() != null ? user.getLongitude().doubleValue() : null;

        return AuthResponse.UserDto.builder()
                .id(user.getId())
                ._id(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .shopName(user.getShopName())
                .address(user.getAddress())
                .latitude(lat)
                .longitude(lon)
                .isVerified(true)
                .build();
    }
}
