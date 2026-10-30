package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    private String email;

    @Column(name = "khata_balance", precision = 12, scale = 2)
    private BigDecimal khataBalance = BigDecimal.ZERO;

    @Column(name = "trust_score")
    private Integer trustScore = 600;

    @Column(name = "khata_score")
    private Integer khataScore = 600;

    @Column(name = "khata_limit", precision = 12, scale = 2)
    private BigDecimal khataLimit = new BigDecimal("3000.00");

    @Column(name = "last_score_update")
    private LocalDateTime lastScoreUpdate = LocalDateTime.now();

    @Column(name = "next_call_date")
    private Long nextCallDate;

    @Column(name = "recovery_status")
    private String recoveryStatus;

    @Column(name = "recovery_notes", columnDefinition = "TEXT")
    private String recoveryNotes;

    @Column(name = "visit_validation")
    private Integer visitValidation = 0;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Column(name = "total_purchases", precision = 12, scale = 2)
    private BigDecimal totalPurchases = BigDecimal.ZERO;

    @Column(name = "preferred_language")
    private String preferredLanguage = "en";

    @Column(name = "preferred_voice_language")
    private String preferredVoiceLanguage = "en";

    @Column(name = "lock_voice_language")
    private Boolean lockVoiceLanguage = false;

    @Column(name = "last_detected_voice_language")
    private String lastDetectedVoiceLanguage;

    @Column(name = "last_voice_language_confidence", precision = 5, scale = 2)
    private BigDecimal lastVoiceLanguageConfidence = BigDecimal.ZERO;

    @Column(name = "voice_language_source")
    private String voiceLanguageSource = "shop_default";

    @Column(name = "voice_language_updated_at")
    private LocalDateTime voiceLanguageUpdatedAt;

    @Column(name = "whatsapp_last_inbound_at")
    private LocalDateTime whatsappLastInboundAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Transient
    private Boolean isLocal;

    public Customer() {}

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
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public BigDecimal getKhataBalance() { return khataBalance; }
    public void setKhataBalance(BigDecimal khataBalance) { this.khataBalance = khataBalance; }
    public BigDecimal getBalance() { return khataBalance; }
    public void setBalance(BigDecimal balance) { this.khataBalance = balance; }

    public Integer getTrustScore() { return trustScore; }
    public void setTrustScore(Integer trustScore) { this.trustScore = trustScore; }
    public Integer getKhataScore() { return khataScore; }
    public void setKhataScore(Integer khataScore) { this.khataScore = khataScore; }
    public BigDecimal getKhataLimit() { return khataLimit; }
    public void setKhataLimit(BigDecimal khataLimit) { this.khataLimit = khataLimit; }
    public LocalDateTime getLastScoreUpdate() { return lastScoreUpdate; }
    public void setLastScoreUpdate(LocalDateTime lastScoreUpdate) { this.lastScoreUpdate = lastScoreUpdate; }
    public Long getNextCallDate() { return nextCallDate; }
    public void setNextCallDate(Long nextCallDate) { this.nextCallDate = nextCallDate; }
    public String getRecoveryStatus() { return recoveryStatus; }
    public void setRecoveryStatus(String recoveryStatus) { this.recoveryStatus = recoveryStatus; }
    public String getRecoveryNotes() { return recoveryNotes; }
    public void setRecoveryNotes(String recoveryNotes) { this.recoveryNotes = recoveryNotes; }
    public Integer getVisitValidation() { return visitValidation; }
    public void setVisitValidation(Integer visitValidation) { this.visitValidation = visitValidation; }
    public Integer getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public BigDecimal getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(BigDecimal totalPurchases) { this.totalPurchases = totalPurchases; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public String getPreferredVoiceLanguage() { return preferredVoiceLanguage; }
    public void setPreferredVoiceLanguage(String preferredVoiceLanguage) { this.preferredVoiceLanguage = preferredVoiceLanguage; }
    public Boolean getLockVoiceLanguage() { return lockVoiceLanguage; }
    public void setLockVoiceLanguage(Boolean lockVoiceLanguage) { this.lockVoiceLanguage = lockVoiceLanguage; }
    public String getLastDetectedVoiceLanguage() { return lastDetectedVoiceLanguage; }
    public void setLastDetectedVoiceLanguage(String lastDetectedVoiceLanguage) { this.lastDetectedVoiceLanguage = lastDetectedVoiceLanguage; }
    public BigDecimal getLastVoiceLanguageConfidence() { return lastVoiceLanguageConfidence; }
    public void setLastVoiceLanguageConfidence(BigDecimal lastVoiceLanguageConfidence) { this.lastVoiceLanguageConfidence = lastVoiceLanguageConfidence; }
    public String getVoiceLanguageSource() { return voiceLanguageSource; }
    public void setVoiceLanguageSource(String voiceLanguageSource) { this.voiceLanguageSource = voiceLanguageSource; }
    public LocalDateTime getVoiceLanguageUpdatedAt() { return voiceLanguageUpdatedAt; }
    public void setVoiceLanguageUpdatedAt(LocalDateTime voiceLanguageUpdatedAt) { this.voiceLanguageUpdatedAt = voiceLanguageUpdatedAt; }
    public LocalDateTime getWhatsappLastInboundAt() { return whatsappLastInboundAt; }
    public void setWhatsappLastInboundAt(LocalDateTime whatsappLastInboundAt) { this.whatsappLastInboundAt = whatsappLastInboundAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getIsLocal() { return isLocal; }
    public void setIsLocal(Boolean isLocal) { this.isLocal = isLocal; }
}
