package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplier_bills")
public class SupplierBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    @JsonIgnore
    private User shopkeeper;

    @OneToMany(mappedBy = "supplierBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SupplierBillItem> items = new ArrayList<>();

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "item_count")
    private Integer itemCount = 0;

    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();

    private String status = "processed";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SupplierBill() {}

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @JsonProperty("shopkeeperId")
    public String getShopkeeperIdString() {
        return shopkeeper != null ? shopkeeper.getId().toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public List<SupplierBillItem> getItems() { return items; }
    public void setItems(List<SupplierBillItem> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
