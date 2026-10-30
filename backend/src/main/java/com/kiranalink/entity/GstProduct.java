package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gst_products")
public class GstProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "normalized_name", nullable = false, unique = true)
    private String normalizedName;

    @Column(name = "hsn_code", nullable = false)
    private String hsnCode;

    @Column(name = "gst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal gstRate = new BigDecimal("5.00");

    @Column(nullable = false)
    private String category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GstProduct() {}

    public GstProduct(String normalizedName, String hsnCode, BigDecimal gstRate, String category) {
        this.normalizedName = normalizedName;
        this.hsnCode = hsnCode;
        this.gstRate = gstRate;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }
    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
