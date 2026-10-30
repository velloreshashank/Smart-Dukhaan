package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "whatsapp_orders")
public class WhatsAppOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    @JsonIgnore
    private User shopkeeper;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_message", nullable = false, columnDefinition = "TEXT")
    private String customerMessage;

    @Column(name = "parsed_text", columnDefinition = "TEXT")
    private String parsedText;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(nullable = false)
    private String channel; // 'whatsapp_text' or 'whatsapp_audio'

    private String status = "confirmed"; // 'received', 'confirmed', 'preparing', 'ready', 'delivered', 'cancelled'

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WhatsAppOrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "payment_mode")
    private String paymentMode = "cash_pickup";

    @Column(name = "source_window_24h")
    private Boolean sourceWindow24h = true;

    @Column(name = "reference_code")
    private String referenceCode;

    @Column(name = "review_state")
    private String reviewState = "none"; // 'none', 'needs_manual_review', 'awaiting_customer_choice'

    @Column(name = "review_reason")
    private String reviewReason;

    @Column(name = "converted_bill_id")
    private Long convertedBillId;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public WhatsAppOrder() {}

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

    @JsonProperty("customerId")
    public Customer getCustomerForJson() {
        return customer;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerMessage() { return customerMessage; }
    public void setCustomerMessage(String customerMessage) { this.customerMessage = customerMessage; }
    public String getParsedText() { return parsedText; }
    public void setParsedText(String parsedText) { this.parsedText = parsedText; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<WhatsAppOrderItem> getItems() { return items; }
    public void setItems(List<WhatsAppOrderItem> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public Boolean getSourceWindow24h() { return sourceWindow24h; }
    public void setSourceWindow24h(Boolean sourceWindow24h) { this.sourceWindow24h = sourceWindow24h; }
    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }
    public String getReviewState() { return reviewState; }
    public void setReviewState(String reviewState) { this.reviewState = reviewState; }
    public String getReviewReason() { return reviewReason; }
    public void setReviewReason(String reviewReason) { this.reviewReason = reviewReason; }
    public Long getConvertedBillId() { return convertedBillId; }
    public void setConvertedBillId(Long convertedBillId) { this.convertedBillId = convertedBillId; }
    public LocalDateTime getConvertedAt() { return convertedAt; }
    public void setConvertedAt(LocalDateTime convertedAt) { this.convertedAt = convertedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
