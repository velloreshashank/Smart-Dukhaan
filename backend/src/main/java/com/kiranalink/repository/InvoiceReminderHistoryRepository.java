package com.kiranalink.repository;

import com.kiranalink.entity.InvoiceReminderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceReminderHistoryRepository extends JpaRepository<InvoiceReminderHistory, Long> {
    List<InvoiceReminderHistory> findByInvoiceId(Long invoiceId);
}
