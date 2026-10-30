package com.kiranalink.repository;

import com.kiranalink.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR c.phoneNumber LIKE CONCAT('%', :query, '%')")
    List<Customer> searchByNameOrPhone(@Param("query") String query);

    @Query("SELECT c FROM Customer c WHERE c.phoneNumber LIKE CONCAT('%', :last10)")
    Optional<Customer> findByPhoneNumberEndingWith(@Param("last10") String last10);

    @Query("SELECT ca.customer FROM CustomerAccount ca WHERE ca.shopkeeper.id = :shopkeeperId")
    List<Customer> findByShopkeeperId(@Param("shopkeeperId") Long shopkeeperId);
}

