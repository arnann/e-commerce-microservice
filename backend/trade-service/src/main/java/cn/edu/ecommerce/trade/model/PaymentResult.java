package cn.edu.ecommerce.trade.model;

import java.math.BigDecimal;

public record PaymentResult(
        Long orderId,
        String channel,
        BigDecimal paidAmount,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus
) {
}
