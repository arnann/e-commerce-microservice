package cn.edu.ecommerce.message.model;

import java.time.Instant;

public record UserMessage(Long id, Long userId, Long orderId, String content, boolean read, Instant createdAt) {

    public UserMessage markRead() {
        return new UserMessage(id, userId, orderId, content, true, createdAt);
    }
}
