package com.kiranalink.service;

import com.kiranalink.entity.InventoryBatch;
import com.kiranalink.entity.Product;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.InventoryBatchRepository;
import com.kiranalink.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryBatchService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;

    public static class BatchConsumptionResult {
        public Long batchId;
        public BigDecimal quantity;

        public BatchConsumptionResult(Long batchId, BigDecimal quantity) {
            this.batchId = batchId;
            this.quantity = quantity;
        }
    }

    public static class FefoResult {
        public List<BatchConsumptionResult> usedBatches = new ArrayList<>();
        public Product product;
    }

    public static Long getDaysToExpiry(LocalDate expiryDate) {
        if (expiryDate == null) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    public static String getRiskBucket(Long daysToExpiry) {
        if (daysToExpiry == null) return null;
        if (daysToExpiry < 0) return "expired";
        if (daysToExpiry <= 3) return "urgent_3d";
        if (daysToExpiry <= 7) return "week_7d";
        if (daysToExpiry <= 30) return "month_30d";
        return null;
    }

    public static String getSuggestedAction(Long daysToExpiry, BigDecimal quantityAvailable) {
        if (daysToExpiry == null) return "none";
        double qty = quantityAvailable != null ? quantityAvailable.doubleValue() : 0.0;
        if (daysToExpiry < 0) return "waste";
        if (daysToExpiry <= 2 && qty >= 15) return "discount";
        if (daysToExpiry <= 2) return "bundle";
        if (daysToExpiry <= 7 && qty >= 25) return "discount";
        if (daysToExpiry <= 7) return "bundle";
        if (daysToExpiry <= 30) return "return";
        return "none";
    }

    @Transactional
    public InventoryBatch addStockBatch(
            Long shopkeeperId,
            Long productId,
            BigDecimal quantity,
            BigDecimal costPricePerUnit,
            BigDecimal sellingPriceSnapshot,
            String batchCode,
            LocalDate mfgDate,
            LocalDate expiryDate,
            Long supplierBillId,
            String source
    ) {
        Product product = productRepository.findByIdAndShopkeeperId(productId, shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        InventoryBatch batch = new InventoryBatch();
        batch.setShopkeeper(product.getShopkeeper());
        batch.setProduct(product);
        batch.setBatchCode(batchCode);
        batch.setMfgDate(mfgDate);
        batch.setExpiryDate(expiryDate);
        batch.setQuantityReceived(quantity);
        batch.setQuantityAvailable(quantity);
        batch.setCostPricePerUnit(costPricePerUnit != null ? costPricePerUnit : product.getCostPrice());
        batch.setSellingPriceSnapshot(sellingPriceSnapshot != null ? sellingPriceSnapshot : product.getPrice());
        batch.setSupplierBillId(supplierBillId);
        batch.setSource(source != null ? source : "manual");
        batch.setStatus("active");

        batch = inventoryBatchRepository.save(batch);

        BigDecimal currentStock = product.getStock() != null ? product.getStock() : BigDecimal.ZERO;
        product.setStock(currentStock.add(quantity));
        productRepository.save(product);

        return batch;
    }

    @Transactional
    public FefoResult consumeProductStockFEFO(Long shopkeeperId, Long productId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Product product = productRepository.findByIdAndShopkeeperId(productId, shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        BigDecimal stock = product.getStock() != null ? product.getStock() : BigDecimal.ZERO;
        if (stock.compareTo(quantity) < 0) {
            throw new BadRequestException("Insufficient stock for " + product.getName() + ". Available: " + stock + ", requested: " + quantity);
        }

        List<InventoryBatch> batches = inventoryBatchRepository.findAvailableBatchesForFefo(shopkeeperId, productId);

        FefoResult result = new FefoResult();

        if (batches.isEmpty()) {
            product.setStock(stock.subtract(quantity));
            result.product = productRepository.save(product);
            return result;
        }

        BigDecimal remaining = quantity;
        for (InventoryBatch batch : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal available = batch.getQuantityAvailable() != null ? batch.getQuantityAvailable() : BigDecimal.ZERO;
            if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal useQty = available.min(remaining);
            BigDecimal newAvailable = available.subtract(useQty);
            batch.setQuantityAvailable(newAvailable);
            if (newAvailable.compareTo(BigDecimal.ZERO) <= 0) {
                batch.setStatus("depleted");
            }

            inventoryBatchRepository.save(batch);
            result.usedBatches.add(new BatchConsumptionResult(batch.getId(), useQty));
            remaining = remaining.subtract(useQty);
        }

        product.setStock(stock.subtract(quantity));
        result.product = productRepository.save(product);
        return result;
    }

    @Transactional
    public void releaseStockBackToBatch(Long shopkeeperId, Long productId, Long batchId, BigDecimal quantity) {
        if (batchId != null) {
            inventoryBatchRepository.findByIdAndShopkeeperId(batchId, shopkeeperId).ifPresent(batch -> {
                batch.setQuantityAvailable(batch.getQuantityAvailable().add(quantity));
                if ("depleted".equals(batch.getStatus())) {
                    batch.setStatus("active");
                }
                inventoryBatchRepository.save(batch);
            });
        }
        productRepository.findByIdAndShopkeeperId(productId, shopkeeperId).ifPresent(p -> {
            p.setStock(p.getStock().add(quantity));
            productRepository.save(p);
        });
    }
}
