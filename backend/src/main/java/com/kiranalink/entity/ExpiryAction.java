package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expiry_actions")
public class ExpiryAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    @JsonIgnore
    private User shopkeeper;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private InventoryBatch batch;

    @Column(name = "days_to_expiry", nullable = false)
    private Integer daysToExpiry;

    @Column(name = "risk_bucket", nullable = false)
    private String riskBucket; // 'urgent_3d', 'week_7d', 'month_30d', 'expired'

    @Column(name = "suggested_action", nullable = false)
    private String suggestedAction; // 'discount', 'bundle', 'return', 'waste', 'none'

    @Column(name = "action_status")
    private String actionStatus = "open"; // 'open', 'in_progress', 'done', 'ignored'

    @Column(name = "action_meta", columnDefinition = "TEXT")
    private String actionMeta;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "acted_at")
    private LocalDateTime actedAt;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt = LocalDateTime.now();

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    @Column(name = "last_notified_bucket")
    private String lastNotifiedBucket;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ExpiryAction() {}

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @JsonProperty("productId")
    public Product getProductForJson() {
        return product;
    }

    @JsonProperty("batchId")
    public InventoryBatch getBatchForJson() {
        return batch;
    }

    @JsonProperty("valueAtRisk")
    public BigDecimal getValueAtRisk() {
        if (batch != null && batch.getQuantityAvailable() != null && product != null && product.getPrice() != null) {
            return batch.getQuantityAvailable().multiply(product.getPrice());
        }
        return BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public InventoryBatch getBatch() { return batch; }
    public void setBatch(InventoryBatch batch) { this.batch = batch; }
    public Integer getDaysToExpiry() { return daysToExpiry; }
    public void setDaysToExpiry(Integer daysToExpiry) { this.daysToExpiry = daysToExpiry; }
    public String getRiskBucket() { return riskBucket; }
    public void setRiskBucket(String riskBucket) { this.riskBucket = riskBucket; }
    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
    public String getActionStatus() { return actionStatus; }
    public void setActionStatus(String actionStatus) { this.actionStatus = actionStatus; }
    public String getActionMeta() { return actionMeta; }
    public void setActionMeta(String actionMeta) { this.actionMeta = actionMeta; }
    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }
    public LocalDateTime getActedAt() { return actedAt; }
    public void setActedAt(LocalDateTime actedAt) { this.actedAt = actedAt; }
    public LocalDateTime getLastEvaluatedAt() { return lastEvaluatedAt; }
    public void setLastEvaluatedAt(LocalDateTime lastEvaluatedAt) { this.lastEvaluatedAt = lastEvaluatedAt; }
    public Boolean getNotificationSent() { return notificationSent; }
    public void setNotificationSent(Boolean notificationSent) { this.notificationSent = notificationSent; }
    public LocalDateTime getNotificationSentAt() { return notificationSentAt; }
    public void setNotificationSentAt(LocalDateTime notificationSentAt) { this.notificationSentAt = notificationSentAt; }
    public String getLastNotifiedBucket() { return lastNotifiedBucket; }
    public void setLastNotifiedBucket(String lastNotifiedBucket) { this.lastNotifiedBucket = lastNotifiedBucket; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
