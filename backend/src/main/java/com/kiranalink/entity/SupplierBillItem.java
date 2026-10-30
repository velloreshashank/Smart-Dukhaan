package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "supplier_bill_items")
public class SupplierBillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_bill_id", nullable = false)
    @JsonIgnore
    private SupplierBill supplierBill;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    private String unit;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    public SupplierBillItem() {}

    public SupplierBillItem(SupplierBill supplierBill, String productName, BigDecimal quantity, String unit, BigDecimal totalAmount, BigDecimal costPrice, BigDecimal sellingPrice) {
        this.supplierBill = supplierBill;
        this.productName = productName;
        this.quantity = quantity;
        this.unit = unit;
        this.totalAmount = totalAmount;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SupplierBill getSupplierBill() { return supplierBill; }
    public void setSupplierBill(SupplierBill supplierBill) { this.supplierBill = supplierBill; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
}
