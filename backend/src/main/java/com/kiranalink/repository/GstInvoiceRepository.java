package com.kiranalink.repository;

import com.kiranalink.entity.GstInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GstInvoiceRepository extends JpaRepository<GstInvoice, Long> {
    List<GstInvoice> findByShopkeeperIdOrderByCreatedAtDesc(Long shopkeeperId);
    List<GstInvoice> findByShopkeeperIdAndInvoiceTypeOrderByCreatedAtDesc(Long shopkeeperId, String invoiceType);
    List<GstInvoice> findByShopkeeperIdAndMonthAndYearOrderByCreatedAtDesc(Long shopkeeperId, Integer month, Integer year);
    List<GstInvoice> findByShopkeeperIdAndInvoiceTypeAndMonthAndYearOrderByCreatedAtDesc(Long shopkeeperId, String invoiceType, Integer month, Integer year);
}
