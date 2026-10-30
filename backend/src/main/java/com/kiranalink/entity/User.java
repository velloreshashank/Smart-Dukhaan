package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "shop_name")
    private String shopName;

    @Column(columnDefinition = "TEXT")
    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "default_voice_language")
    private String defaultVoiceLanguage = "en";

    @Column(name = "fallback_voice_language")
    private String fallbackVoiceLanguage = "en";

    @Column(name = "voice_language_policy")
    private String voiceLanguagePolicy = "hybrid";

    @Column(name = "enable_voice_language_menu")
    private Boolean enableVoiceLanguageMenu = true;

    @Column(name = "supported_voice_languages")
    private String supportedVoiceLanguages = "en,hi,te";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public User() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getDefaultVoiceLanguage() { return defaultVoiceLanguage; }
    public void setDefaultVoiceLanguage(String defaultVoiceLanguage) { this.defaultVoiceLanguage = defaultVoiceLanguage; }
    public String getFallbackVoiceLanguage() { return fallbackVoiceLanguage; }
    public void setFallbackVoiceLanguage(String fallbackVoiceLanguage) { this.fallbackVoiceLanguage = fallbackVoiceLanguage; }
    public String getVoiceLanguagePolicy() { return voiceLanguagePolicy; }
    public void setVoiceLanguagePolicy(String voiceLanguagePolicy) { this.voiceLanguagePolicy = voiceLanguagePolicy; }
    public Boolean getEnableVoiceLanguageMenu() { return enableVoiceLanguageMenu; }
    public void setEnableVoiceLanguageMenu(Boolean enableVoiceLanguageMenu) { this.enableVoiceLanguageMenu = enableVoiceLanguageMenu; }
    public String getSupportedVoiceLanguages() { return supportedVoiceLanguages; }
    public void setSupportedVoiceLanguages(String supportedVoiceLanguages) { this.supportedVoiceLanguages = supportedVoiceLanguages; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
