package com.kiranalink.repository;

import com.kiranalink.entity.GstLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GstLedgerRepository extends JpaRepository<GstLedger, Long> {
    List<GstLedger> findByShopkeeperIdOrderByCreatedAtDesc(Long shopkeeperId);
    List<GstLedger> findByShopkeeperIdAndTypeOrderByCreatedAtDesc(Long shopkeeperId, String type);
    List<GstLedger> findByShopkeeperIdAndMonthAndYearOrderByCreatedAtDesc(Long shopkeeperId, Integer month, Integer year);
    List<GstLedger> findByShopkeeperIdAndTypeAndMonthAndYearOrderByCreatedAtDesc(Long shopkeeperId, String type, Integer month, Integer year);

    @Query("SELECT gl FROM GstLedger gl WHERE gl.shopkeeper.id = :shopkeeperId AND gl.type = :type AND gl.month = :month AND gl.year = :year")
    List<GstLedger> findForSummary(@Param("shopkeeperId") Long shopkeeperId, @Param("type") String type, @Param("month") Integer month, @Param("year") Integer year);
}
