package cn.edu.ecommerce.trade.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderView(
        Long id,
        Long userId,
        List<OrderItemView> items,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt
) {
    public OrderView withStatus(OrderStatus nextStatus) {
        return new OrderView(id, userId, items, totalAmount, nextStatus, createdAt);
    }
}
