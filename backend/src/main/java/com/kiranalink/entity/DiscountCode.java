package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_codes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_dc_code_shop", columnNames = {"code", "shopkeeper_id"})
})
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    @JsonIgnore
    private User shopkeeper;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(columnDefinition = "TEXT")
    private String description = "";

    @Column(name = "discount_type", nullable = false)
    private String discountType; // 'percentage' or 'fixed'

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_purchase", precision = 12, scale = 2)
    private BigDecimal minPurchase = BigDecimal.ZERO;

    @Column(name = "max_uses")
    private Integer maxUses = 100;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "valid_from")
    private LocalDateTime validFrom = LocalDateTime.now();

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_for")
    private String createdFor = "manual"; // 'expiry', 'manual', 'promotional'

    @Column(name = "linked_batch_id")
    private Long linkedBatchId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public DiscountCode() {}

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

    @JsonProperty("productId")
    public Product getProductForJson() {
        return product;
    }

    @JsonProperty("shopkeeperId")
    public String getShopkeeperIdString() {
        return shopkeeper != null ? shopkeeper.getId().toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinPurchase() { return minPurchase; }
    public void setMinPurchase(BigDecimal minPurchase) { this.minPurchase = minPurchase; }
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getCreatedFor() { return createdFor; }
    public void setCreatedFor(String createdFor) { this.createdFor = createdFor; }
    public Long getLinkedBatchId() { return linkedBatchId; }
    public void setLinkedBatchId(Long linkedBatchId) { this.linkedBatchId = linkedBatchId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
