package com.kiranalink.repository;

import com.kiranalink.entity.GstProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GstProductRepository extends JpaRepository<GstProduct, Long> {
    Optional<GstProduct> findByNormalizedName(String normalizedName);
    List<GstProduct> findByNormalizedNameIn(List<String> names);
    List<GstProduct> findByNormalizedNameContainingIgnoreCase(String name);
}
