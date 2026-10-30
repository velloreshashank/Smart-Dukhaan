package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_call_sessions")
public class VoiceCallSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_sid", nullable = false, unique = true)
    private String callSid;

    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    private String stage = "OPENING";
    private String status = "active"; // 'active', 'completed', 'failed'

    @Column(name = "turn_count")
    private Integer turnCount = 0;

    @Column(name = "partial_amount_now", precision = 12, scale = 2)
    private BigDecimal partialAmountNow = BigDecimal.ZERO;

    @Column(name = "remaining_amount", precision = 12, scale = 2)
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    @Column(name = "promised_date")
    private LocalDateTime promisedDate;

    @Column(name = "outcome_intent")
    private String outcomeIntent = "UNKNOWN";

    @Column(precision = 5, scale = 2)
    private BigDecimal confidence = BigDecimal.ZERO;

    @Column(name = "final_summary", columnDefinition = "TEXT")
    private String finalSummary;

    @Column(name = "detected_language")
    private String detectedLanguage = "en";

    @Column(name = "language_confidence", precision = 5, scale = 2)
    private BigDecimal languageConfidence = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public VoiceCallSession() {}

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCallSid() { return callSid; }
    public void setCallSid(String callSid) { this.callSid = callSid; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTurnCount() { return turnCount; }
    public void setTurnCount(Integer turnCount) { this.turnCount = turnCount; }
    public BigDecimal getPartialAmountNow() { return partialAmountNow; }
    public void setPartialAmountNow(BigDecimal partialAmountNow) { this.partialAmountNow = partialAmountNow; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }
    public LocalDateTime getPromisedDate() { return promisedDate; }
    public void setPromisedDate(LocalDateTime promisedDate) { this.promisedDate = promisedDate; }
    public String getOutcomeIntent() { return outcomeIntent; }
    public void setOutcomeIntent(String outcomeIntent) { this.outcomeIntent = outcomeIntent; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getFinalSummary() { return finalSummary; }
    public void setFinalSummary(String finalSummary) { this.finalSummary = finalSummary; }
    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }
    public BigDecimal getLanguageConfidence() { return languageConfidence; }
    public void setLanguageConfidence(BigDecimal languageConfidence) { this.languageConfidence = languageConfidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
