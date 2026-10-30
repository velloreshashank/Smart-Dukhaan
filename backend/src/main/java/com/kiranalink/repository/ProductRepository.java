package com.kiranalink.repository;

import com.kiranalink.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopkeeperId(Long shopkeeperId);
    Optional<Product> findByIdAndShopkeeperId(Long id, Long shopkeeperId);
    void deleteByShopkeeperId(Long shopkeeperId);

    @Query("SELECT p FROM Product p WHERE p.shopkeeper.id = :shopkeeperId AND (p.hsnCode IS NULL OR p.gstRate IS NULL OR p.gstRate = 0)")
    List<Product> findUnclassifiedByShopkeeperId(@Param("shopkeeperId") Long shopkeeperId);
}
