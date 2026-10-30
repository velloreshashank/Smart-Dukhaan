package com.kiranalink.repository;

import com.kiranalink.entity.WhatsAppOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WhatsAppOrderRepository extends JpaRepository<WhatsAppOrder, Long> {
    List<WhatsAppOrder> findByShopkeeperIdOrderByCreatedAtDesc(Long shopkeeperId);
    List<WhatsAppOrder> findByShopkeeperIdAndStatusOrderByCreatedAtDesc(Long shopkeeperId, String status);
    Optional<WhatsAppOrder> findByIdAndShopkeeperId(Long id, Long shopkeeperId);
    Optional<WhatsAppOrder> findByReferenceCode(String referenceCode);

    long countByShopkeeperIdAndCreatedAtAfter(Long shopkeeperId, LocalDateTime after);
    long countByShopkeeperIdAndReviewState(Long shopkeeperId, String reviewState);

    @Query("SELECT COUNT(wo) FROM WhatsAppOrder wo WHERE wo.shopkeeper.id = :shopkeeperId AND wo.reviewState = 'none' AND wo.convertedBillId IS NULL AND wo.totalAmount > 0 AND wo.status IN ('confirmed', 'preparing', 'ready', 'delivered')")
    long countReadyToBill(@Param("shopkeeperId") Long shopkeeperId);
}
