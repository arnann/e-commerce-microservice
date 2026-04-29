package cn.edu.ecommerce.message.model;

public record CreateMessageRequest(Long orderId, String content) {
}
