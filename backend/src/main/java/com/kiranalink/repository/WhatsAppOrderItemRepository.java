package com.kiranalink.repository;

import com.kiranalink.entity.WhatsAppOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WhatsAppOrderItemRepository extends JpaRepository<WhatsAppOrderItem, Long> {
    List<WhatsAppOrderItem> findByOrderId(Long orderId);
    void deleteByOrderId(Long orderId);
}
