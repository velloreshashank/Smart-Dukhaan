package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gst_ledger")
public class GstLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    @JsonIgnore
    private User shopkeeper;

    @Column(nullable = false)
    private String type; // 'input' (purchase) or 'output' (sale)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gst_invoice_id", nullable = false)
    @JsonIgnore
    private GstInvoice gstInvoice;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type")
    private String referenceType; // 'Bill' or 'SupplierBill'

    @Column(name = "total_base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalBaseAmount;

    @Column(name = "total_cgst", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCGST;

    @Column(name = "total_sgst", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSGST;

    @Column(name = "total_gst", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGST;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GstLedger() {}

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @JsonProperty("gstInvoiceId")
    public String getGstInvoiceIdString() {
        return gstInvoice != null ? gstInvoice.getId().toString() : null;
    }

    @JsonProperty("shopkeeperId")
    public String getShopkeeperIdString() {
        return shopkeeper != null ? shopkeeper.getId().toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public GstInvoice getGstInvoice() { return gstInvoice; }
    public void setGstInvoice(GstInvoice gstInvoice) { this.gstInvoice = gstInvoice; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public BigDecimal getTotalBaseAmount() { return totalBaseAmount; }
    public void setTotalBaseAmount(BigDecimal totalBaseAmount) { this.totalBaseAmount = totalBaseAmount; }
    public BigDecimal getTotalCGST() { return totalCGST; }
    public void setTotalCGST(BigDecimal totalCGST) { this.totalCGST = totalCGST; }
    public BigDecimal getTotalSGST() { return totalSGST; }
    public void setTotalSGST(BigDecimal totalSGST) { this.totalSGST = totalSGST; }
    public BigDecimal getTotalGST() { return totalGST; }
    public void setTotalGST(BigDecimal totalGST) { this.totalGST = totalGST; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
