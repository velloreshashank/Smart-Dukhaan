package com.kiranalink.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;

        @JsonProperty("_id")
        private String _id;

        private String name;
        private String username;
        private String email;
        private String phone;
        private String shopName;
        private String address;
        private Double latitude;
        private Double longitude;
        private Boolean isVerified;
    }
}

