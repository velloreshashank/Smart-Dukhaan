package com.kiranalink.service;

import com.kiranalink.dto.bill.BillCreateRequest;
import com.kiranalink.dto.bill.BillItemDto;
import com.kiranalink.entity.*;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillItemRepository billItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAccountRepository customerAccountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryBatchService inventoryBatchService;

    @Mock
    private KhataScoreService khataScoreService;

    @Mock
    private GstService gstService;

    @InjectMocks
    private BillService billService;

    private User shopkeeper;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        shopkeeper = new User();
        shopkeeper.setId(1L);
        shopkeeper.setName("Sharma General Store");

        customer = new Customer();
        customer.setId(5L);
        customer.setName("Vikram Singh");
        customer.setPhoneNumber("+919988776655");
        customer.setKhataLimit(BigDecimal.valueOf(2000));
        customer.setKhataBalance(BigDecimal.valueOf(200));

        product = new Product();
        product.setId(20L);
        product.setShopkeeper(shopkeeper);
        product.setName("Fortune Sunflower Oil 1L");
        product.setPrice(BigDecimal.valueOf(140));
        product.setStock(BigDecimal.valueOf(50));
    }

    @Test
    @DisplayName("Should successfully create a cash bill and persist bill items")
    void testSuccessfulCashSale() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(shopkeeper));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdAndShopkeeperId(20L, 1L)).thenReturn(Optional.of(product));
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill b = invocation.getArgument(0);
            b.setId(1001L);
            return b;
        });

        BillCreateRequest request = BillCreateRequest.builder()
                .customerId(5L)
                .paymentType("cash")
                .items(List.of(
                        BillItemDto.builder()
                                .productId("20")
                                .name("Fortune Sunflower Oil 1L")
                                .quantity(BigDecimal.valueOf(2))
                                .price(BigDecimal.valueOf(140))
                                .build()
                ))
                .build();

        Bill created = billService.createBillInternal(1L, request, "cash");

        assertNotNull(created);
        assertEquals(BigDecimal.valueOf(280), created.getTotalAmount());
        assertEquals("cash", created.getPaymentType());
        verify(inventoryBatchService, times(1)).consumeProductStockFEFO(eq(1L), eq(20L), eq(BigDecimal.valueOf(2)));
        verify(billRepository, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Ledger sale exceeding khata credit limit throws BadRequestException")
    void testLedgerSaleExceedingCreditLimitThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(shopkeeper));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdAndShopkeeperId(20L, 1L)).thenReturn(Optional.of(product));

        // Current balance 1900, limit 2000. New bill is 280 -> Total 2180 > 2000 limit
        CustomerAccount account = new CustomerAccount(customer, shopkeeper, BigDecimal.valueOf(1900));
        when(customerAccountRepository.findByCustomerIdAndShopkeeperId(5L, 1L)).thenReturn(Optional.of(account));

        BillCreateRequest request = BillCreateRequest.builder()
                .customerId(5L)
                .paymentType("ledger")
                .items(List.of(
                        BillItemDto.builder()
                                .productId("20")
                                .name("Fortune Sunflower Oil 1L")
                                .quantity(BigDecimal.valueOf(2))
                                .price(BigDecimal.valueOf(140))
                                .build()
                ))
                .build();

        assertThrows(BadRequestException.class, () -> {
            billService.createBillInternal(1L, request, "ledger");
        }, "Should reject credit sale that breaches credit limit");
    }
}
