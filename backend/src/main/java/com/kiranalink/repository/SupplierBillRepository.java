package com.kiranalink.repository;

import com.kiranalink.entity.SupplierBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SupplierBillRepository extends JpaRepository<SupplierBill, Long> {
    List<SupplierBill> findByShopkeeperIdOrderByCreatedAtDesc(Long shopkeeperId);

    @Query("SELECT COALESCE(SUM(sb.totalAmount), 0) FROM SupplierBill sb WHERE sb.shopkeeper.id = :shopkeeperId AND sb.createdAt >= :startDate AND sb.createdAt < :endDate")
    BigDecimal sumTotalByShopkeeperAndDateRange(@Param("shopkeeperId") Long shopkeeperId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
