package cn.edu.ecommerce.trade.model;

public record AddCartItemRequest(Long userId, Long productId, int quantity) {
}
