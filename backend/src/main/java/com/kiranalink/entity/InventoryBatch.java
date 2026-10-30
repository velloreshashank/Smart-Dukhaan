package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_batches")
public class InventoryBatch {

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

    @Column(name = "batch_code")
    private String batchCode;

    @Column(name = "mfg_date")
    private LocalDate mfgDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "quantity_received", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityReceived;

    @Column(name = "quantity_available", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityAvailable;

    @Column(name = "cost_price_per_unit", precision = 12, scale = 2)
    private BigDecimal costPricePerUnit;

    @Column(name = "selling_price_snapshot", precision = 12, scale = 2)
    private BigDecimal sellingPriceSnapshot;

    @Column(name = "supplier_bill_id")
    private Long supplierBillId;

    private String source = "manual";

    private String status = "active"; // 'active', 'depleted', 'expired', 'returned'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public InventoryBatch() {}

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public LocalDate getMfgDate() { return mfgDate; }
    public void setMfgDate(LocalDate mfgDate) { this.mfgDate = mfgDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public BigDecimal getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(BigDecimal quantityReceived) { this.quantityReceived = quantityReceived; }
    public BigDecimal getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(BigDecimal quantityAvailable) { this.quantityAvailable = quantityAvailable; }
    public BigDecimal getCostPricePerUnit() { return costPricePerUnit; }
    public void setCostPricePerUnit(BigDecimal costPricePerUnit) { this.costPricePerUnit = costPricePerUnit; }
    public BigDecimal getSellingPriceSnapshot() { return sellingPriceSnapshot; }
    public void setSellingPriceSnapshot(BigDecimal sellingPriceSnapshot) { this.sellingPriceSnapshot = sellingPriceSnapshot; }
    public Long getSupplierBillId() { return supplierBillId; }
    public void setSupplierBillId(Long supplierBillId) { this.supplierBillId = supplierBillId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
