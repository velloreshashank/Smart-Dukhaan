package com.kiranalink.repository;

import com.kiranalink.entity.WasteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WasteLogRepository extends JpaRepository<WasteLog, Long> {
    List<WasteLog> findByShopkeeperIdOrderByLoggedAtDesc(Long shopkeeperId);
    List<WasteLog> findByShopkeeperIdAndLoggedAtBetweenOrderByLoggedAtDesc(Long shopkeeperId, LocalDateTime from, LocalDateTime to);
}
