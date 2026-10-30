package com.kiranalink.service;

import com.kiranalink.dto.waste.WasteLogRequest;
import com.kiranalink.entity.ExpiryAction;
import com.kiranalink.entity.InventoryBatch;
import com.kiranalink.entity.Product;
import com.kiranalink.entity.WasteLog;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.ExpiryActionRepository;
import com.kiranalink.repository.InventoryBatchRepository;
import com.kiranalink.repository.ProductRepository;
import com.kiranalink.repository.WasteLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WasteService {

    private final WasteLogRepository wasteLogRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;
    private final ExpiryActionRepository expiryActionRepository;

    public List<WasteLog> getWasteHistory(Long shopkeeperId) {
        return wasteLogRepository.findByShopkeeperIdOrderByLoggedAtDesc(shopkeeperId);
    }

    @Transactional
    public WasteLog logWaste(Long shopkeeperId, WasteLogRequest request) {
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Waste quantity must be greater than zero");
        }

        Product product = productRepository.findByIdAndShopkeeperId(request.getProductId(), shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        BigDecimal unitCost = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
        InventoryBatch batch = null;

        if (request.getBatchId() != null) {
            batch = inventoryBatchRepository.findByIdAndShopkeeperId(request.getBatchId(), shopkeeperId)
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + request.getBatchId()));

            if (batch.getQuantityAvailable().compareTo(request.getQuantity()) < 0) {
                throw new BadRequestException("Waste quantity exceeds available batch quantity");
            }

            if (batch.getCostPricePerUnit() != null) {
                unitCost = batch.getCostPricePerUnit();
            }

            BigDecimal newBatchQty = batch.getQuantityAvailable().subtract(request.getQuantity());
            batch.setQuantityAvailable(newBatchQty);
            if (newBatchQty.compareTo(BigDecimal.ZERO) <= 0) {
                batch.setStatus("depleted");
            }
            inventoryBatchRepository.save(batch);

            List<ExpiryAction> openActions = expiryActionRepository.findAllOpenByBatchId(shopkeeperId, batch.getId());
            for (ExpiryAction ea : openActions) {
                ea.setActionStatus("done");
                ea.setActedAt(LocalDateTime.now());
                expiryActionRepository.save(ea);
            }
        }

        BigDecimal currentStock = product.getStock() != null ? product.getStock() : BigDecimal.ZERO;
        product.setStock(currentStock.subtract(request.getQuantity()).max(BigDecimal.ZERO));
        productRepository.save(product);

        BigDecimal estimatedLoss = request.getEstimatedLossAmount() != null ?
                request.getEstimatedLossAmount() : unitCost.multiply(request.getQuantity()).setScale(2, RoundingMode.HALF_UP);

        WasteLog wasteLog = new WasteLog();
        wasteLog.setShopkeeper(product.getShopkeeper());
        wasteLog.setProduct(product);
        wasteLog.setBatch(batch);
        wasteLog.setReason(request.getReason());
        wasteLog.setQuantity(request.getQuantity());
        wasteLog.setUnitCost(unitCost);
        wasteLog.setEstimatedLoss(estimatedLoss);
        wasteLog.setDisposalMode("discarded");
        wasteLog.setNotes(request.getNotes());
        wasteLog.setLoggedBy(shopkeeperId);
        wasteLog.setLoggedAt(LocalDateTime.now());

        return wasteLogRepository.save(wasteLog);
    }

    public Map<String, Object> getWasteSummary(Long shopkeeperId) {
        List<WasteLog> logs = wasteLogRepository.findByShopkeeperIdOrderByLoggedAtDesc(shopkeeperId);

        BigDecimal totalLoss = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (WasteLog w : logs) {
            if (w.getEstimatedLoss() != null) {
                totalLoss = totalLoss.add(w.getEstimatedLoss());
            }
            if (w.getQuantity() != null) {
                totalQuantity = totalQuantity.add(w.getQuantity());
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLogs", logs.size());
        summary.put("totalLossAmount", totalLoss);
        summary.put("totalQuantityWasted", totalQuantity);
        summary.put("history", logs);
        return summary;
    }
}
