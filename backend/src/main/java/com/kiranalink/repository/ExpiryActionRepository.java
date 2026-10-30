package com.kiranalink.repository;

import com.kiranalink.entity.ExpiryAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpiryActionRepository extends JpaRepository<ExpiryAction, Long> {
    List<ExpiryAction> findByShopkeeperIdOrderByDaysToExpiryAsc(Long shopkeeperId);
    List<ExpiryAction> findByShopkeeperIdAndActionStatusOrderByDaysToExpiryAsc(Long shopkeeperId, String actionStatus);
    List<ExpiryAction> findByShopkeeperIdAndRiskBucketOrderByDaysToExpiryAsc(Long shopkeeperId, String riskBucket);
    List<ExpiryAction> findByShopkeeperIdAndRiskBucketAndActionStatusOrderByDaysToExpiryAsc(Long shopkeeperId, String riskBucket, String actionStatus);
    Optional<ExpiryAction> findByIdAndShopkeeperId(Long id, Long shopkeeperId);

    @Query("SELECT ea FROM ExpiryAction ea WHERE ea.shopkeeper.id = :shopkeeperId AND ea.batch.id = :batchId AND ea.actionStatus IN ('open', 'in_progress')")
    Optional<ExpiryAction> findOpenByBatchId(@Param("shopkeeperId") Long shopkeeperId, @Param("batchId") Long batchId);

    @Query("SELECT ea FROM ExpiryAction ea WHERE ea.shopkeeper.id = :shopkeeperId AND ea.batch.id = :batchId AND ea.actionStatus IN ('open', 'in_progress')")
    List<ExpiryAction> findAllOpenByBatchId(@Param("shopkeeperId") Long shopkeeperId, @Param("batchId") Long batchId);
}
