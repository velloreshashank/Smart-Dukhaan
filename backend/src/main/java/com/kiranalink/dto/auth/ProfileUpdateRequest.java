package com.kiranalink.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String name;
    private String avatar;
    private String shopName;
    private String phone;
    private String address;
    private Double latitude;
    private Double longitude;
    private String defaultVoiceLanguage;
    private String fallbackVoiceLanguage;
    private String voiceLanguagePolicy;
    private Boolean enableVoiceLanguageMenu;
    private List<String> supportedVoiceLanguages;
}

