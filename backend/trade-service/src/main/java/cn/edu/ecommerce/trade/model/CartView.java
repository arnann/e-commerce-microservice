package cn.edu.ecommerce.trade.model;

import java.math.BigDecimal;
import java.util.List;

public record CartView(Long userId, List<CartItem> items, BigDecimal totalAmount) {
}
