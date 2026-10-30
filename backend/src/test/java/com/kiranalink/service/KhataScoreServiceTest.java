package com.kiranalink.service;

import com.kiranalink.entity.Customer;
import com.kiranalink.entity.CustomerAccount;
import com.kiranalink.entity.LedgerEntry;
import com.kiranalink.repository.CustomerAccountRepository;
import com.kiranalink.repository.CustomerRepository;
import com.kiranalink.repository.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KhataScoreServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAccountRepository customerAccountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private KhataScoreService khataScoreService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Ramesh Kumar");
        customer.setPhoneNumber("+919876543210");
        customer.setKhataScore(600);
        customer.setTrustScore(600);
        customer.setKhataLimit(BigDecimal.valueOf(3000));
        customer.setKhataBalance(BigDecimal.valueOf(500));
        customer.setCreatedAt(LocalDateTime.now().minusDays(60));
    }

    @Test
    @DisplayName("Should recalculate score within 300 to 900 range")
    void testScoreBoundedRange() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(ledgerEntryRepository.findByCustomerIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(customerAccountRepository.findByCustomerId(1L)).thenReturn(List.of());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        khataScoreService.recalculateGlobalKhataScore(1L);

        int updatedScore = customer.getKhataScore();
        assertTrue(updatedScore >= 300 && updatedScore <= 900, "Score must stay between 300 and 900, but was: " + updatedScore);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Should reward customer with higher Khata limit when repayment history is strong")
    void testExcellentRepaymentBoostsScoreAndLimit() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        List<LedgerEntry> entries = new ArrayList<>();
        // Simulate high velocity of successful repayments (credit entries)
        for (int i = 0; i < 15; i++) {
            LedgerEntry debit = new LedgerEntry(null, customer, null, BigDecimal.valueOf(200), "debit", "ledger", "settled");
            debit.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
            entries.add(debit);

            LedgerEntry credit = new LedgerEntry(null, customer, null, BigDecimal.valueOf(200), "credit", "cash", "settled");
            credit.setCreatedAt(LocalDateTime.now().minusDays(29 - i));
            entries.add(credit);
        }

        when(ledgerEntryRepository.findByCustomerIdOrderByCreatedAtAsc(1L)).thenReturn(entries);

        CustomerAccount account = new CustomerAccount(customer, null, BigDecimal.valueOf(100));
        when(customerAccountRepository.findByCustomerId(1L)).thenReturn(List.of(account));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        khataScoreService.recalculateGlobalKhataScore(1L);

        int updatedScore = customer.getKhataScore();
        assertTrue(updatedScore >= 600, "Consistent on-time repayments should yield score >= 600, got: " + updatedScore);
        assertTrue(customer.getKhataLimit().compareTo(BigDecimal.valueOf(3000)) >= 0, "Khata limit should increase or maintain");
    }
}
