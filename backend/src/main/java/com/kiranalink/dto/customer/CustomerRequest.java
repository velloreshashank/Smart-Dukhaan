package com.kiranalink.dto.customer;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class CustomerRequest {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String name;
    private String email;
    private BigDecimal khataBalance;
    private Integer trustScore;
    private String preferredVoiceLanguage;
    private Boolean lockVoiceLanguage;

    public CustomerRequest() {}

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public BigDecimal getKhataBalance() { return khataBalance; }
    public void setKhataBalance(BigDecimal khataBalance) { this.khataBalance = khataBalance; }
    public Integer getTrustScore() { return trustScore; }
    public void setTrustScore(Integer trustScore) { this.trustScore = trustScore; }
    public String getPreferredVoiceLanguage() { return preferredVoiceLanguage; }
    public void setPreferredVoiceLanguage(String preferredVoiceLanguage) { this.preferredVoiceLanguage = preferredVoiceLanguage; }
    public Boolean getLockVoiceLanguage() { return lockVoiceLanguage; }
    public void setLockVoiceLanguage(Boolean lockVoiceLanguage) { this.lockVoiceLanguage = lockVoiceLanguage; }
}
