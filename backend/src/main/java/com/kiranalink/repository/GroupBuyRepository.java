package com.kiranalink.repository;

import com.kiranalink.entity.GroupBuy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {
    List<GroupBuy> findByStatusOrderByCreatedAtDesc(String status);
    List<GroupBuy> findAllByOrderByCreatedAtDesc();
}
