package com.kiranalink.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_buys")
public class GroupBuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shopkeeper_id", nullable = false)
    private User shopkeeper;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "market_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal marketPrice;

    @Column(name = "deal_price", precision = 12, scale = 2)
    private BigDecimal dealPrice;

    @Column(name = "target_units")
    private Integer targetUnits = 10;

    @Column(name = "current_units")
    private Integer currentUnits = 0;

    @Column(name = "image_url")
    private String imageUrl;

    private String category = "General";

    @Column(name = "anchor_shop")
    private String anchorShop = "Local Hub";

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "ai_insight", columnDefinition = "TEXT")
    private String aiInsight;

    private String status = "active"; // 'active', 'completed', 'expired'

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_buy_members", joinColumns = @JoinColumn(name = "group_buy_id"))
    @Column(name = "customer_id")
    private List<String> members = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GroupBuy() {}

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @JsonProperty("shopkeeperId")
    public User getShopkeeperForJson() {
        return shopkeeper;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getShopkeeper() { return shopkeeper; }
    public void setShopkeeper(User shopkeeper) { this.shopkeeper = shopkeeper; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getMarketPrice() { return marketPrice; }
    public void setMarketPrice(BigDecimal marketPrice) { this.marketPrice = marketPrice; }
    public BigDecimal getDealPrice() { return dealPrice; }
    public void setDealPrice(BigDecimal dealPrice) { this.dealPrice = dealPrice; }
    public Integer getTargetUnits() { return targetUnits; }
    public void setTargetUnits(Integer targetUnits) { this.targetUnits = targetUnits; }
    public Integer getCurrentUnits() { return currentUnits; }
    public void setCurrentUnits(Integer currentUnits) { this.currentUnits = currentUnits; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAnchorShop() { return anchorShop; }
    public void setAnchorShop(String anchorShop) { this.anchorShop = anchorShop; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getAiInsight() { return aiInsight; }
    public void setAiInsight(String aiInsight) { this.aiInsight = aiInsight; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
