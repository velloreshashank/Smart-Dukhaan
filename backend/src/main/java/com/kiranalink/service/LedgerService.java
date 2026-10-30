package com.kiranalink.service;

import com.kiranalink.dto.ledger.LedgerPaymentRequest;
import com.kiranalink.entity.Customer;
import com.kiranalink.entity.CustomerAccount;
import com.kiranalink.entity.LedgerEntry;
import com.kiranalink.entity.User;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.CustomerAccountRepository;
import com.kiranalink.repository.CustomerRepository;
import com.kiranalink.repository.LedgerEntryRepository;
import com.kiranalink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final KhataScoreService khataScoreService;

    public List<LedgerEntry> getEntriesByCustomer(Long shopkeeperId, Long customerId) {
        return ledgerEntryRepository.findByShopkeeperIdAndCustomerIdOrderByCreatedAtDesc(shopkeeperId, customerId);
    }

    @Transactional
    public LedgerEntry recordPayment(Long shopkeeperId, LedgerPaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Repayment amount must be greater than zero");
        }

        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        Long customerId;
        try {
            customerId = Long.parseLong(request.getCustomerId());
        } catch (Exception e) {
            throw new BadRequestException("Invalid customer ID: " + request.getCustomerId());
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        CustomerAccount account = customerAccountRepository.findByCustomerIdAndShopkeeperId(customer.getId(), shopkeeperId)
                .orElseThrow(() -> new BadRequestException("Customer account for this shopkeeper not found"));

        LedgerEntry entry = new LedgerEntry(
                shopkeeper,
                customer,
                null,
                request.getAmount(),
                "credit",
                request.getPaymentMode() != null ? request.getPaymentMode() : "cash",
                "settled"
        );

        entry = ledgerEntryRepository.save(entry);

        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.subtract(request.getAmount());
        account.setBalance(newBalance);
        customerAccountRepository.save(account);

        customer.setKhataBalance(newBalance);
        customerRepository.save(customer);

        try {
            khataScoreService.recalculateGlobalKhataScore(customer.getId());
        } catch (Exception e) {
            log.warn("Failed to recalculate khata score after payment: {}", e.getMessage());
        }

        return entry;
    }
}
