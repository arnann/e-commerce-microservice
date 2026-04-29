package cn.edu.ecommerce.product.model;

import java.math.BigDecimal;

public record CreateProductRequest(
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        int stock
) {
}
