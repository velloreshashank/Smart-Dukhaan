package com.kiranalink.repository;

import com.kiranalink.entity.VoiceCallSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoiceCallSessionRepository extends JpaRepository<VoiceCallSession, Long> {
    Optional<VoiceCallSession> findByCallSid(String callSid);
    Optional<VoiceCallSession> findFirstByInvoiceIdOrderByCreatedAtDesc(String invoiceId);
}
