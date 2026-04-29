package cn.edu.ecommerce.trade.model;

import java.math.BigDecimal;

public record OrderItemView(Long productId, String productName, BigDecimal price, int quantity, BigDecimal amount) {
}
