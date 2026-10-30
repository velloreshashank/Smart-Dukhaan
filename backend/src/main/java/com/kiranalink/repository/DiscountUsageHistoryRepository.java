package com.kiranalink.repository;

import com.kiranalink.entity.DiscountUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountUsageHistoryRepository extends JpaRepository<DiscountUsageHistory, Long> {
    List<DiscountUsageHistory> findByDiscountId(Long discountId);
}
