package com.kiranalink.service;

import com.kiranalink.dto.expiry.BatchCreateRequest;
import com.kiranalink.dto.expiry.ExpiryActionUpdateRequest;
import com.kiranalink.entity.ExpiryAction;
import com.kiranalink.entity.InventoryBatch;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.ExpiryActionRepository;
import com.kiranalink.repository.InventoryBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiryService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final ExpiryActionRepository expiryActionRepository;
    private final InventoryBatchService inventoryBatchService;

    public InventoryBatch createBatch(Long shopkeeperId, BatchCreateRequest request) {
        return inventoryBatchService.addStockBatch(
                shopkeeperId,
                request.getProductId(),
                request.getQuantity(),
                request.getPurchasePrice(),
                request.getMrp(),
                request.getBatchNumber(),
                request.getManufactureDate(),
                request.getExpiryDate(),
                null,
                "manual"
        );
    }

    public List<InventoryBatch> getBatches(Long shopkeeperId, String status, Long productId) {
        List<InventoryBatch> batches;
        if (productId != null) {
            batches = inventoryBatchRepository.findByShopkeeperIdAndProductIdOrderByExpiryDateAscCreatedAtDesc(shopkeeperId, productId);
        } else if (status != null && !status.isBlank()) {
            batches = inventoryBatchRepository.findByShopkeeperIdAndStatusOrderByExpiryDateAscCreatedAtDesc(shopkeeperId, status);
        } else {
            batches = inventoryBatchRepository.findByShopkeeperIdOrderByExpiryDateAscCreatedAtDesc(shopkeeperId);
        }
        return batches;
    }

    @Transactional
    public InventoryBatch updateBatch(Long shopkeeperId, Long batchId, String status, String batchCode, LocalDate mfgDate, LocalDate expiryDate) {
        InventoryBatch batch = inventoryBatchRepository.findByIdAndShopkeeperId(batchId, shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));

        if (status != null && !status.isBlank()) batch.setStatus(status.toLowerCase());
        if (batchCode != null) batch.setBatchCode(batchCode);
        if (mfgDate != null) batch.setMfgDate(mfgDate);
        if (expiryDate != null) batch.setExpiryDate(expiryDate);

        return inventoryBatchRepository.save(batch);
    }

    @Transactional
    public Map<String, Object> recomputeQueue(Long shopkeeperId) {
        List<InventoryBatch> batches = inventoryBatchRepository.findByShopkeeperIdOrderByExpiryDateAscCreatedAtDesc(shopkeeperId)
                .stream()
                .filter(b -> List.of("active", "expired").contains(b.getStatus()) &&
                        b.getQuantityAvailable() != null && b.getQuantityAvailable().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        int touched = 0;
        for (InventoryBatch batch : batches) {
            Long days = InventoryBatchService.getDaysToExpiry(batch.getExpiryDate());
            if (days == null) continue;

            if (days < 0 && !"expired".equals(batch.getStatus())) {
                batch.setStatus("expired");
                inventoryBatchRepository.save(batch);
            }

            String riskBucket = InventoryBatchService.getRiskBucket(days);
            if (riskBucket == null) continue;

            String suggestedAction = InventoryBatchService.getSuggestedAction(days, batch.getQuantityAvailable());

            Optional<ExpiryAction> existing = expiryActionRepository.findOpenByBatchId(shopkeeperId, batch.getId());
            ExpiryAction action = existing.orElseGet(() -> {
                ExpiryAction ea = new ExpiryAction();
                ea.setShopkeeper(batch.getShopkeeper());
                ea.setProduct(batch.getProduct());
                ea.setBatch(batch);
                ea.setActionStatus("open");
                return ea;
            });

            action.setDaysToExpiry(days.intValue());
            action.setRiskBucket(riskBucket);
            action.setSuggestedAction(suggestedAction);
            action.setLastEvaluatedAt(LocalDateTime.now());

            expiryActionRepository.save(action);
            touched++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("touched", touched);
        result.put("scanned", batches.size());
        return result;
    }

    public Map<String, Object> getQueue(Long shopkeeperId, String bucket, String status) {
        List<ExpiryAction> actions;
        String targetStatus = (status != null && !status.isBlank()) ? status : "open";

        if (bucket != null && !bucket.isBlank()) {
            if ("all".equalsIgnoreCase(targetStatus)) {
                actions = expiryActionRepository.findByShopkeeperIdAndRiskBucketOrderByDaysToExpiryAsc(shopkeeperId, bucket);
            } else {
                actions = expiryActionRepository.findByShopkeeperIdAndRiskBucketAndActionStatusOrderByDaysToExpiryAsc(shopkeeperId, bucket, targetStatus);
            }
        } else {
            if ("all".equalsIgnoreCase(targetStatus)) {
                actions = expiryActionRepository.findByShopkeeperIdOrderByDaysToExpiryAsc(shopkeeperId);
            } else {
                actions = expiryActionRepository.findByShopkeeperIdAndActionStatusOrderByDaysToExpiryAsc(shopkeeperId, targetStatus);
            }
        }

        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal totalValueAtRisk = BigDecimal.ZERO;
        long urgent3dCount = 0;
        long week7dCount = 0;
        long month30dCount = 0;
        long expiredCount = 0;

        for (ExpiryAction a : actions) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", a.getId());
            itemMap.put("_id", a.getId().toString());
            itemMap.put("daysToExpiry", a.getDaysToExpiry());
            itemMap.put("riskBucket", a.getRiskBucket());
            itemMap.put("suggestedAction", a.getSuggestedAction());
            itemMap.put("actionStatus", a.getActionStatus());
            itemMap.put("product", a.getProduct());
            itemMap.put("batch", a.getBatch());

            BigDecimal valueAtRisk = a.getValueAtRisk() != null ? a.getValueAtRisk() : BigDecimal.ZERO;
            itemMap.put("valueAtRisk", valueAtRisk);
            totalValueAtRisk = totalValueAtRisk.add(valueAtRisk);

            if ("urgent_3d".equalsIgnoreCase(a.getRiskBucket())) urgent3dCount++;
            else if ("week_7d".equalsIgnoreCase(a.getRiskBucket())) week7dCount++;
            else if ("month_30d".equalsIgnoreCase(a.getRiskBucket())) month30dCount++;
            else if ("expired".equalsIgnoreCase(a.getRiskBucket())) expiredCount++;

            items.add(itemMap);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("urgent_3d", urgent3dCount);
        summary.put("week_7d", week7dCount);
        summary.put("month_30d", month30dCount);
        summary.put("expired", expiredCount);
        summary.put("totalValueAtRisk", totalValueAtRisk);

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("items", items);
        return response;
    }

    @Transactional
    public ExpiryAction updateAction(Long shopkeeperId, Long actionId, ExpiryActionUpdateRequest request) {
        ExpiryAction action = expiryActionRepository.findByIdAndShopkeeperId(actionId, shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Expiry action not found with id: " + actionId));

        action.setActionStatus(request.getStatus().toLowerCase());
        action.setActedAt(LocalDateTime.now());
        if (request.getResolutionNotes() != null) {
            action.setActionMeta(request.getResolutionNotes());
        }

        return expiryActionRepository.save(action);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void nightlyExpiryScan() {
        log.info("[ExpiryEngine] Starting automated nightly batch expiry scan");
        List<InventoryBatch> activeBatches = inventoryBatchRepository.findAllActiveForScheduler();
        for (InventoryBatch b : activeBatches) {
            if (b.getExpiryDate() != null) {
                Long days = InventoryBatchService.getDaysToExpiry(b.getExpiryDate());
                if (days != null && days < 0 && !"expired".equals(b.getStatus())) {
                    b.setStatus("expired");
                    inventoryBatchRepository.save(b);
                }
            }
        }
    }
}
