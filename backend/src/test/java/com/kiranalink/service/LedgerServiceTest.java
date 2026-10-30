package com.kiranalink.service;

import com.kiranalink.dto.ledger.LedgerPaymentRequest;
import com.kiranalink.entity.Customer;
import com.kiranalink.entity.CustomerAccount;
import com.kiranalink.entity.LedgerEntry;
import com.kiranalink.entity.User;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.repository.CustomerAccountRepository;
import com.kiranalink.repository.CustomerRepository;
import com.kiranalink.repository.LedgerEntryRepository;
import com.kiranalink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private CustomerAccountRepository customerAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KhataScoreService khataScoreService;

    @InjectMocks
    private LedgerService ledgerService;

    private User shopkeeper;
    private Customer customer;
    private CustomerAccount account;

    @BeforeEach
    void setUp() {
        shopkeeper = new User();
        shopkeeper.setId(1L);

        customer = new Customer();
        customer.setId(10L);
        customer.setName("Anita Desai");
        customer.setPhoneNumber("+919123456789");
        customer.setKhataBalance(BigDecimal.valueOf(1500));

        account = new CustomerAccount(customer, shopkeeper, BigDecimal.valueOf(1500));
        account.setId(100L);
    }

    @Test
    @DisplayName("Recording customer repayment reduces outstanding balance and triggers score update")
    void testRecordPaymentReducesBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(shopkeeper));
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(customerAccountRepository.findByCustomerIdAndShopkeeperId(10L, 1L)).thenReturn(Optional.of(account));
        when(ledgerEntryRepository.save(any(LedgerEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LedgerPaymentRequest request = LedgerPaymentRequest.builder()
                .customerId("10")
                .amount(BigDecimal.valueOf(500))
                .paymentMode("upi")
                .build();

        LedgerEntry entry = ledgerService.recordPayment(1L, request);

        assertNotNull(entry);
        assertEquals(BigDecimal.valueOf(500), entry.getAmount());
        assertEquals("credit", entry.getType());
        assertEquals("settled", entry.getStatus());

        assertEquals(BigDecimal.valueOf(1000), account.getBalance(), "Shopkeeper account balance should be reduced from 1500 to 1000");
        assertEquals(BigDecimal.valueOf(1000), customer.getKhataBalance(), "Global khata balance should be updated to 1000");

        verify(customerAccountRepository, times(1)).save(account);
        verify(customerRepository, times(1)).save(customer);
        verify(khataScoreService, times(1)).recalculateGlobalKhataScore(10L);
    }

    @Test
    @DisplayName("Zero or negative repayment amount throws BadRequestException")
    void testZeroPaymentThrows() {
        LedgerPaymentRequest zeroRequest = LedgerPaymentRequest.builder()
                .customerId("10")
                .amount(BigDecimal.ZERO)
                .build();

        assertThrows(BadRequestException.class, () -> {
            ledgerService.recordPayment(1L, zeroRequest);
        });
    }
}
