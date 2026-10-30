package com.kiranalink.repository;

import com.kiranalink.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByShopkeeperIdOrderByExpiryDateAscCreatedAtDesc(Long shopkeeperId);
    List<InventoryBatch> findByShopkeeperIdAndStatusOrderByExpiryDateAscCreatedAtDesc(Long shopkeeperId, String status);
    List<InventoryBatch> findByShopkeeperIdAndProductIdOrderByExpiryDateAscCreatedAtDesc(Long shopkeeperId, Long productId);
    List<InventoryBatch> findByShopkeeperIdAndProductIdAndStatusOrderByExpiryDateAscCreatedAtDesc(Long shopkeeperId, Long productId, String status);
    Optional<InventoryBatch> findByIdAndShopkeeperId(Long id, Long shopkeeperId);

    @Query("SELECT b FROM InventoryBatch b WHERE b.shopkeeper.id = :shopkeeperId AND b.product.id = :productId AND b.status IN ('active', 'expired') AND b.quantityAvailable > 0 ORDER BY b.expiryDate ASC, b.createdAt ASC")
    List<InventoryBatch> findAvailableBatchesForFefo(@Param("shopkeeperId") Long shopkeeperId, @Param("productId") Long productId);

    @Query("SELECT b FROM InventoryBatch b WHERE b.status IN ('active', 'expired') AND b.quantityAvailable > 0")
    List<InventoryBatch> findAllActiveForScheduler();
}
