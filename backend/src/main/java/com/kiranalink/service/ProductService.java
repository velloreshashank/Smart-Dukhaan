package com.kiranalink.service;

import com.kiranalink.dto.gst.GstClassifyResponse;
import com.kiranalink.dto.product.ProductRequest;
import com.kiranalink.entity.Product;
import com.kiranalink.entity.User;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final GstService gstService;
    private final InventoryBatchService inventoryBatchService;

    public List<Product> getProducts(Long shopkeeperId, String search, String category, Boolean lowStock) {
        List<Product> products = productRepository.findByShopkeeperId(shopkeeperId);

        return products.stream()
                .filter(p -> {
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase();
                        boolean nameMatch = p.getName() != null && p.getName().toLowerCase().contains(s);
                        if (!nameMatch) return false;
                    }
                    if (category != null && !category.isBlank()) {
                        if (p.getCategory() == null || !p.getCategory().equalsIgnoreCase(category)) return false;
                    }
                    if (Boolean.TRUE.equals(lowStock)) {
                        BigDecimal stock = p.getStock() != null ? p.getStock() : BigDecimal.ZERO;
                        if (stock.compareTo(BigDecimal.valueOf(10)) > 0) return false;
                    }
                    return true;
                })
                .toList();
    }

    public Product getProductById(Long shopkeeperId, Long productId) {
        return productRepository.findByIdAndShopkeeperId(productId, shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    @Transactional
    public Product createProduct(Long shopkeeperId, ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Product name is required");
        }

        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        BigDecimal gstRate = request.getGstRate();
        String hsnCode = request.getHsnCode();

        if (gstRate == null || hsnCode == null || hsnCode.isBlank()) {
            try {
                GstClassifyResponse classified = gstService.classifyProduct(request.getName());
                if (gstRate == null) gstRate = classified.getGstRate();
                if (hsnCode == null) hsnCode = classified.getHsnCode();
            } catch (Exception e) {
                log.warn("GST classification fallback failed for {}: {}", request.getName(), e.getMessage());
                if (gstRate == null) gstRate = BigDecimal.valueOf(5);
                if (hsnCode == null) hsnCode = "2106";
            }
        }

        BigDecimal stock = request.getStock() != null ? request.getStock() : BigDecimal.ZERO;
        BigDecimal costPrice = request.getCostPrice() != null ? request.getCostPrice() : request.getPrice().multiply(BigDecimal.valueOf(0.85));

        Product product = new Product(
                shopkeeper,
                request.getName(),
                request.getPrice(),
                stock,
                request.getCategory() != null ? request.getCategory() : "General",
                request.getUnit() != null ? request.getUnit() : "unit",
                request.getIcon() != null ? request.getIcon() : "📦"
        );
        product.setCostPrice(costPrice);
        product.setGstRate(gstRate != null ? gstRate : BigDecimal.ZERO);
        product.setHsnCode(hsnCode);

        product = productRepository.save(product);

        if (stock.compareTo(BigDecimal.ZERO) > 0) {
            try {
                inventoryBatchService.addStockBatch(
                        shopkeeperId,
                        product.getId(),
                        stock,
                        product.getCostPrice(),
                        product.getPrice(),
                        "INIT-" + System.currentTimeMillis() % 100000,
                        null,
                        null,
                        null,
                        "initial_creation"
                );
            } catch (Exception e) {
                log.warn("Could not create initial inventory batch: {}", e.getMessage());
            }
        }

        return product;
    }

    @Transactional
    public Product updateProduct(Long shopkeeperId, Long productId, ProductRequest request) {
        Product product = getProductById(shopkeeperId, productId);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getIcon() != null) product.setIcon(request.getIcon());
        if (request.getGstRate() != null) product.setGstRate(request.getGstRate());
        if (request.getHsnCode() != null) product.setHsnCode(request.getHsnCode());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long shopkeeperId, Long productId) {
        Product product = getProductById(shopkeeperId, productId);
        productRepository.delete(product);
    }

    @Transactional
    public int seedStarterProducts(Long shopkeeperId) {
        List<Product> existing = productRepository.findByShopkeeperId(shopkeeperId);
        if (!existing.isEmpty()) {
            return existing.size();
        }

        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        record StarterItem(String name, double price, double stock, String category, String unit, String icon, double gstRate, String hsn) {}

        List<StarterItem> items = List.of(
                new StarterItem("Basmati Rice", 90, 100, "Grain", "kg", "🌾", 0, "1006"),
                new StarterItem("Atta (Wheat Flour)", 45, 500, "Flour", "kg", "🌾", 0, "1101"),
                new StarterItem("Sona Masuri Rice", 60, 200, "Grain", "kg", "🍚", 0, "1006"),
                new StarterItem("Toor Dal", 165, 80, "Pulses", "kg", "🍲", 0, "0713"),
                new StarterItem("Moong Dal", 110, 100, "Pulses", "kg", "🍲", 0, "0713"),
                new StarterItem("Sunflower Oil", 135, 100, "Oil", "litre", "🌻", 5, "1512"),
                new StarterItem("Mustard Oil", 160, 80, "Oil", "litre", "🛢️", 5, "1514"),
                new StarterItem("Fresh Milk", 62, 200, "Dairy", "litre", "🥛", 0, "0401"),
                new StarterItem("Sugar", 44, 500, "Grocery", "kg", "🍬", 5, "1701"),
                new StarterItem("Salt", 22, 300, "Grocery", "kg", "🧂", 0, "2501"),
                new StarterItem("Turmeric Powder", 280, 40, "Spices", "kg", "🟡", 5, "0910"),
                new StarterItem("Red Chili Powder", 350, 40, "Spices", "kg", "🌶️", 5, "0904"),
                new StarterItem("Detergent Powder", 110, 150, "Cleaning", "kg", "🧺", 18, "3402"),
                new StarterItem("Loose Tea", 350, 60, "Beverage", "kg", "☕", 5, "0902")
        );

        int count = 0;
        for (StarterItem item : items) {
            Product p = new Product(
                    shopkeeper,
                    item.name(),
                    BigDecimal.valueOf(item.price()),
                    BigDecimal.valueOf(item.stock()),
                    item.category(),
                    item.unit(),
                    item.icon()
            );
            p.setCostPrice(BigDecimal.valueOf(item.price() * 0.85));
            p.setGstRate(BigDecimal.valueOf(item.gstRate()));
            p.setHsnCode(item.hsn());
            productRepository.save(p);
            count++;
        }

        return count;
    }
}
