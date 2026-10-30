package com.kiranalink.repository;

import com.kiranalink.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByCustomerIdAndShopkeeperIdOrderByCreatedAtDesc(Long customerId, Long shopkeeperId);
    List<LedgerEntry> findByShopkeeperIdAndCustomerIdOrderByCreatedAtDesc(Long shopkeeperId, Long customerId);
    List<LedgerEntry> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
}
