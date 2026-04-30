package cn.edu.ecommerce.product.model;

import java.math.BigDecimal;

public record UpdateProductRequest(
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        int stock,
        ProductStatus status
) {
}
