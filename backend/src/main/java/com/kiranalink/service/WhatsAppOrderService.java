package com.kiranalink.service;

import com.kiranalink.dto.bill.BillCreateRequest;
import com.kiranalink.dto.bill.BillItemDto;
import com.kiranalink.dto.whatsapp.OrderConvertToBillRequest;
import com.kiranalink.dto.whatsapp.OrderItemsUpdateRequest;
import com.kiranalink.dto.whatsapp.OrderStatusRequest;
import com.kiranalink.entity.Bill;
import com.kiranalink.entity.Product;
import com.kiranalink.entity.WhatsAppOrder;
import com.kiranalink.entity.WhatsAppOrderItem;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.ProductRepository;
import com.kiranalink.repository.WhatsAppOrderItemRepository;
import com.kiranalink.repository.WhatsAppOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppOrderService {

    private final WhatsAppOrderRepository whatsAppOrderRepository;
    private final WhatsAppOrderItemRepository whatsAppOrderItemRepository;
    private final ProductRepository productRepository;
    private final BillService billService;

    public List<WhatsAppOrder> getOrders(Long shopkeeperId, String status) {
        List<WhatsAppOrder> orders;
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) {
            orders = whatsAppOrderRepository.findByShopkeeperIdAndStatusOrderByCreatedAtDesc(shopkeeperId, status.toLowerCase());
        } else {
            orders = whatsAppOrderRepository.findByShopkeeperIdOrderByCreatedAtDesc(shopkeeperId);
        }
        return orders;
    }

    public WhatsAppOrder getOrderById(Long shopkeeperId, Long orderId) {
        WhatsAppOrder order = whatsAppOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp order not found with id: " + orderId));
        if (!order.getShopkeeper().getId().equals(shopkeeperId)) {
            throw new BadRequestException("Unauthorized access to this order");
        }
        return order;
    }

    @Transactional
    public WhatsAppOrder updateStatus(Long shopkeeperId, Long orderId, OrderStatusRequest request) {
        WhatsAppOrder order = getOrderById(shopkeeperId, orderId);
        order.setStatus(request.getStatus().toLowerCase());
        return whatsAppOrderRepository.save(order);
    }

    @Transactional
    public WhatsAppOrder updateOrderItems(Long shopkeeperId, Long orderId, OrderItemsUpdateRequest request) {
        WhatsAppOrder order = getOrderById(shopkeeperId, orderId);

        whatsAppOrderItemRepository.deleteByOrderId(order.getId());

        BigDecimal newTotal = BigDecimal.ZERO;
        List<WhatsAppOrderItem> itemsToSave = new ArrayList<>();

        for (OrderItemsUpdateRequest.OrderItemUpdateDto itemDto : request.getItems()) {
            BigDecimal qty = itemDto.getQuantity() != null ? itemDto.getQuantity() : BigDecimal.ONE;
            BigDecimal price = itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = price.multiply(qty);
            newTotal = newTotal.add(lineTotal);

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElse(null);

            WhatsAppOrderItem item = new WhatsAppOrderItem(
                    order,
                    product,
                    itemDto.getName(),
                    qty,
                    price,
                    lineTotal
            );
            itemsToSave.add(item);
        }

        whatsAppOrderItemRepository.saveAll(itemsToSave);
        order.setItems(itemsToSave);
        order.setTotalAmount(newTotal);

        if ("received".equalsIgnoreCase(order.getStatus()) && !itemsToSave.isEmpty()) {
            order.setStatus("confirmed");
        }

        return whatsAppOrderRepository.save(order);
    }

    @Transactional
    public Map<String, Object> convertToBill(Long shopkeeperId, Long orderId, OrderConvertToBillRequest request) {
        WhatsAppOrder order = getOrderById(shopkeeperId, orderId);

        if (order.getConvertedBillId() != null) {
            Bill existingBill = billService.getBillById(order.getConvertedBillId(), shopkeeperId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("alreadyConverted", true);
            result.put("bill", existingBill);
            result.put("order", order);
            return result;
        }

        List<BillItemDto> billItems = new ArrayList<>();
        if (order.getItems() != null) {
            for (WhatsAppOrderItem oi : order.getItems()) {
                billItems.add(BillItemDto.builder()
                        .productId(oi.getProduct() != null ? oi.getProduct().getId().toString() : null)
                        .name(oi.getName())
                        .quantity(oi.getQuantity())
                        .price(oi.getUnitPrice())
                        .build());
            }
        }

        String paymentType = (request != null && request.getPaymentType() != null) ?
                request.getPaymentType() : "cash";

        BillCreateRequest billRequest = BillCreateRequest.builder()
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerPhoneNumber(order.getCustomerPhone())
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : "Customer")
                .paymentType(paymentType)
                .items(billItems)
                .build();

        Bill bill = billService.createBillInternal(shopkeeperId, billRequest, paymentType);

        order.setConvertedBillId(bill.getId());
        order.setConvertedAt(LocalDateTime.now());
        order.setStatus("delivered");
        whatsAppOrderRepository.save(order);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("bill", bill);
        response.put("order", order);
        return response;
    }
}
