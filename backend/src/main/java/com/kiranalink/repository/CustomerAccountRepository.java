package com.kiranalink.repository;

import com.kiranalink.entity.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {
    List<CustomerAccount> findByShopkeeperId(Long shopkeeperId);
    List<CustomerAccount> findByCustomerId(Long customerId);
    Optional<CustomerAccount> findByCustomerIdAndShopkeeperId(Long customerId, Long shopkeeperId);
    void deleteByCustomerIdAndShopkeeperId(Long customerId, Long shopkeeperId);

    @Query("SELECT ca FROM CustomerAccount ca WHERE ca.shopkeeper.id = :shopkeeperId AND ca.balance > 0")
    List<CustomerAccount> findOverdueAccountsByShopkeeperId(@Param("shopkeeperId") Long shopkeeperId);

    @Query("SELECT ca FROM CustomerAccount ca WHERE ca.customer.id = :customerId AND ca.balance > 0")
    List<CustomerAccount> findOverdueAccountsByCustomerId(@Param("customerId") Long customerId);
}
