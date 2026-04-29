package cn.edu.ecommerce.trade.service;

import java.math.BigDecimal;

public record ProductSnapshot(Long productId, String name, BigDecimal price, int stock) {
}
