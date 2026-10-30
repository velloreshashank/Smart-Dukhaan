package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_usage_history")
public class DiscountUsageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    @JsonIgnore
    private DiscountCode discount;

    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "used_at", updatable = false)
    private LocalDateTime usedAt = LocalDateTime.now();

    public DiscountUsageHistory() {}

    public DiscountUsageHistory(DiscountCode discount, Long billId, Long customerId, BigDecimal discountAmount) {
        this.discount = discount;
        this.billId = billId;
        this.customerId = customerId;
        this.discountAmount = discountAmount;
        this.usedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DiscountCode getDiscount() { return discount; }
    public void setDiscount(DiscountCode discount) { this.discount = discount; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
