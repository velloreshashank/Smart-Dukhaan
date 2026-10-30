package com.kiranalink.service;

import com.kiranalink.entity.InventoryBatch;
import com.kiranalink.entity.Product;
import com.kiranalink.entity.User;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.repository.InventoryBatchRepository;
import com.kiranalink.repository.ProductRepository;
import com.kiranalink.repository.UserRepository;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryBatchServiceTest {

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InventoryBatchService inventoryBatchService;

    private User shopkeeper;
    private Product product;
    private InventoryBatch batch1;
    private InventoryBatch batch2;

    @BeforeEach
    void setUp() {
        shopkeeper = new User();
        shopkeeper.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setShopkeeper(shopkeeper);
        product.setName("Aashirvaad Atta 5kg");
        product.setPrice(BigDecimal.valueOf(250));
        product.setStock(BigDecimal.valueOf(15));

        // Batch 1: Expires in 10 days, quantity 5
        batch1 = new InventoryBatch();
        batch1.setId(101L);
        batch1.setShopkeeper(shopkeeper);
        batch1.setProduct(product);
        batch1.setBatchCode("BATCH-001");
        batch1.setQuantityReceived(BigDecimal.valueOf(5));
        batch1.setQuantityAvailable(BigDecimal.valueOf(5));
        batch1.setCostPricePerUnit(BigDecimal.valueOf(200));
        batch1.setExpiryDate(LocalDate.now().plusDays(10));
        batch1.setStatus("active");

        // Batch 2: Expires in 40 days, quantity 10
        batch2 = new InventoryBatch();
        batch2.setId(102L);
        batch2.setShopkeeper(shopkeeper);
        batch2.setProduct(product);
        batch2.setBatchCode("BATCH-002");
        batch2.setQuantityReceived(BigDecimal.valueOf(10));
        batch2.setQuantityAvailable(BigDecimal.valueOf(10));
        batch2.setCostPricePerUnit(BigDecimal.valueOf(200));
        batch2.setExpiryDate(LocalDate.now().plusDays(40));
        batch2.setStatus("active");
    }

    @Test
    @DisplayName("FEFO: Consumes earlier expiring batch first")
    void testConsumeFEFOEarliestFirst() {
        when(productRepository.findByIdAndShopkeeperId(10L, 1L)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findAvailableBatchesForFefo(1L, 10L))
                .thenReturn(new ArrayList<>(List.of(batch1, batch2)));

        // Consume 3 units (less than batch1 capacity of 5)
        inventoryBatchService.consumeProductStockFEFO(1L, 10L, BigDecimal.valueOf(3));

        assertEquals(BigDecimal.valueOf(2), batch1.getQuantityAvailable(), "Batch 1 should have 2 remaining");
        assertEquals(BigDecimal.valueOf(10), batch2.getQuantityAvailable(), "Batch 2 should remain untouched at 10");
        assertEquals(BigDecimal.valueOf(12), product.getStock(), "Product stock should decrease to 12");
        verify(inventoryBatchRepository, times(1)).save(batch1);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("FEFO: Depletes batch 1 and consumes remainder from batch 2")
    void testConsumeFEFOMultiBatch() {
        when(productRepository.findByIdAndShopkeeperId(10L, 1L)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findAvailableBatchesForFefo(1L, 10L))
                .thenReturn(new ArrayList<>(List.of(batch1, batch2)));

        // Consume 8 units (5 from batch1 + 3 from batch2)
        inventoryBatchService.consumeProductStockFEFO(1L, 10L, BigDecimal.valueOf(8));

        assertEquals(BigDecimal.ZERO, batch1.getQuantityAvailable(), "Batch 1 should be completely exhausted (0)");
        assertEquals(BigDecimal.valueOf(7), batch2.getQuantityAvailable(), "Batch 2 should have 7 remaining");
        assertEquals(BigDecimal.valueOf(7), product.getStock(), "Product total stock should be 7");
    }

    @Test
    @DisplayName("Insufficient stock should throw BadRequestException")
    void testConsumeInsufficientStockThrowsException() {
        when(productRepository.findByIdAndShopkeeperId(10L, 1L)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findAvailableBatchesForFefo(1L, 10L))
                .thenReturn(new ArrayList<>(List.of(batch1, batch2))); // total = 15

        assertThrows(BadRequestException.class, () -> {
            inventoryBatchService.consumeProductStockFEFO(1L, 10L, BigDecimal.valueOf(20));
        }, "Should throw BadRequestException when requesting 20 units with only 15 available");
    }
}
