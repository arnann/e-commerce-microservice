package cn.edu.ecommerce.product.model;

import java.time.Instant;

public record Category(Long id, String name, Instant createdAt) {
}
