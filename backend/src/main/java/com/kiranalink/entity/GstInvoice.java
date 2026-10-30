package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gst_invoices")
public class GstInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    @JsonIgnore
    private User shopkeeper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    @Column(name = "bill_id")
    private Long billId;

    @OneToMany(mappedBy = "gstInvoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GstInvoiceItem> items = new ArrayList<>();

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

    @Column(name = "invoice_type", nullable = false)
    private String invoiceType; // 'sale' or 'purchase'

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GstInvoice() {}

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @JsonProperty("customerId")
    public String getCustomerIdString() {
        return customer != null ? customer.getId().toString() : null;
    }

    @JsonProperty("shopkeeperId")
    public String getShopkeeperIdString() {
        return shopkeeper != null ? shopkeeper.getId().toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public List<GstInvoiceItem> getItems() { return items; }
    public void setItems(List<GstInvoiceItem> items) { this.items = items; }
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
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
