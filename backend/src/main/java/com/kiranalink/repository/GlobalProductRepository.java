package com.kiranalink.repository;

import com.kiranalink.entity.GlobalProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalProductRepository extends JpaRepository<GlobalProduct, Long> {
}
