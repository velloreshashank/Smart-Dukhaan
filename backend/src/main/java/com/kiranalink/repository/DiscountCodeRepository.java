package com.kiranalink.repository;

import com.kiranalink.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    List<DiscountCode> findByShopkeeperIdOrderByCreatedAtDesc(Long shopkeeperId);
    List<DiscountCode> findByShopkeeperIdAndIsActiveOrderByCreatedAtDesc(Long shopkeeperId, Boolean isActive);
    List<DiscountCode> findByShopkeeperIdAndCreatedForOrderByCreatedAtDesc(Long shopkeeperId, String createdFor);
    List<DiscountCode> findByShopkeeperIdAndIsActiveAndCreatedForOrderByCreatedAtDesc(Long shopkeeperId, Boolean isActive, String createdFor);
    Optional<DiscountCode> findByCodeAndShopkeeperId(String code, Long shopkeeperId);
    Optional<DiscountCode> findByCode(String code);
}
