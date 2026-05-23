package cn.edu.ecommerce.product.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductSummary(
        Long id,
        Long categoryId,
        String name,
        String description,
        String imageUrl,
        BigDecimal price,
        int stock,
        ProductStatus status,
        Instant createdAt
) {
    public ProductSummary withStatus(ProductStatus nextStatus) {
        return new ProductSummary(id, categoryId, name, description, imageUrl, price, stock, nextStatus, createdAt);
    }

    public ProductSummary withStock(int nextStock) {
        return new ProductSummary(id, categoryId, name, description, imageUrl, price, nextStock, status, createdAt);
    }
}
