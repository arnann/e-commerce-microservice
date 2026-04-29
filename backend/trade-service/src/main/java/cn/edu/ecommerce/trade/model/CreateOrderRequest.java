package cn.edu.ecommerce.trade.model;

import java.util.Map;

public record CreateOrderRequest(Long userId, Map<Long, Integer> productQuantities) {
}
