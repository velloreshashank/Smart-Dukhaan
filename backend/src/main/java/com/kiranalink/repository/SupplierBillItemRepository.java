package com.kiranalink.repository;

import com.kiranalink.entity.SupplierBillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierBillItemRepository extends JpaRepository<SupplierBillItem, Long> {
    List<SupplierBillItem> findBySupplierBillId(Long supplierBillId);
}
