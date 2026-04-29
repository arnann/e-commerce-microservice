package cn.edu.ecommerce.message.model;

import java.time.Instant;

public record Notice(Long id, String title, String content, Long publisherId, boolean published, Instant createdAt) {
}
