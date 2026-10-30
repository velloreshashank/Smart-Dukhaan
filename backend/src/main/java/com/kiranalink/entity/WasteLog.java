package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "waste_logs")
public class WasteLog {

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

    @Column(nullable = false)
    private String reason; // 'expired', 'damaged', 'spoilage', 'leakage', 'return_rejected', 'other'

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "estimated_loss", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedLoss;

    @Column(name = "disposal_mode")
    private String disposalMode = "discarded";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "logged_by", nullable = false)
    private Long loggedBy;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt = LocalDateTime.now();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public WasteLog() {}

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public InventoryBatch getBatch() { return batch; }
    public void setBatch(InventoryBatch batch) { this.batch = batch; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getEstimatedLoss() { return estimatedLoss; }
    public void setEstimatedLoss(BigDecimal estimatedLoss) { this.estimatedLoss = estimatedLoss; }
    public String getDisposalMode() { return disposalMode; }
    public void setDisposalMode(String disposalMode) { this.disposalMode = disposalMode; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getLoggedBy() { return loggedBy; }
    public void setLoggedBy(Long loggedBy) { this.loggedBy = loggedBy; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
