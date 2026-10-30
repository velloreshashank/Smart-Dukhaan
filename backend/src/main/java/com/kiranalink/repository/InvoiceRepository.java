package com.kiranalink.repository;

import com.kiranalink.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceId(String invoiceId);
    List<Invoice> findAllByOrderByCreatedAtDesc();
    List<Invoice> findByStatusOrderByDueDateAsc(String status);

    @Query("SELECT i FROM Invoice i WHERE i.clientPhone LIKE CONCAT('%', :last10)")
    List<Invoice> findByClientPhoneEndingWith(@Param("last10") String last10);

    @Query("SELECT i FROM Invoice i WHERE i.clientPhone LIKE CONCAT('%', :last10) AND i.status IN ('unpaid', 'overdue', 'promised', 'disputed')")
    Optional<Invoice> findActiveByClientPhoneEndingWith(@Param("last10") String last10);
}
