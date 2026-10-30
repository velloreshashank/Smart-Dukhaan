package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false, unique = true)
    @JsonProperty("invoice_id")
    private String invoiceId;

    @Column(name = "client_name", nullable = false)
    @JsonProperty("client_name")
    private String clientName;

    @Column(name = "client_email")
    @JsonProperty("client_email")
    private String clientEmail;

    @Column(name = "client_phone", nullable = false)
    @JsonProperty("client_phone")
    private String clientPhone;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    @JsonProperty("due_date")
    private LocalDateTime dueDate;

    private String status = "unpaid"; // 'paid', 'unpaid', 'overdue', 'disputed', 'promised'

    @Column(name = "reminder_level")
    @JsonProperty("reminder_level")
    private Integer reminderLevel = 0;

    @Column(name = "last_contacted_at")
    @JsonProperty("last_contacted_at")
    private LocalDateTime lastContactedAt;

    @Column(name = "payment_link")
    @JsonProperty("payment_link")
    private String paymentLink = "";

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonProperty("reminder_history")
    private List<InvoiceReminderHistory> reminderHistory = new ArrayList<>();

    @Column(name = "promised_date")
    @JsonProperty("promised_date")
    private LocalDateTime promisedDate;

    @Column(name = "next_retry_at")
    @JsonProperty("next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_intent")
    @JsonProperty("last_intent")
    private String lastIntent = "UNKNOWN";

    @Column(name = "ai_confidence", precision = 5, scale = 2)
    @JsonProperty("ai_confidence")
    private BigDecimal aiConfidence = BigDecimal.ZERO;

    @Column(name = "no_speech_count")
    @JsonProperty("no_speech_count")
    private Integer noSpeechCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Invoice() {}

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
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getReminderLevel() { return reminderLevel; }
    public void setReminderLevel(Integer reminderLevel) { this.reminderLevel = reminderLevel; }
    public LocalDateTime getLastContactedAt() { return lastContactedAt; }
    public void setLastContactedAt(LocalDateTime lastContactedAt) { this.lastContactedAt = lastContactedAt; }
    public String getPaymentLink() { return paymentLink; }
    public void setPaymentLink(String paymentLink) { this.paymentLink = paymentLink; }
    public List<InvoiceReminderHistory> getReminderHistory() { return reminderHistory; }
    public void setReminderHistory(List<InvoiceReminderHistory> reminderHistory) { this.reminderHistory = reminderHistory; }
    public LocalDateTime getPromisedDate() { return promisedDate; }
    public void setPromisedDate(LocalDateTime promisedDate) { this.promisedDate = promisedDate; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public String getLastIntent() { return lastIntent; }
    public void setLastIntent(String lastIntent) { this.lastIntent = lastIntent; }
    public BigDecimal getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(BigDecimal aiConfidence) { this.aiConfidence = aiConfidence; }
    public Integer getNoSpeechCount() { return noSpeechCount; }
    public void setNoSpeechCount(Integer noSpeechCount) { this.noSpeechCount = noSpeechCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
