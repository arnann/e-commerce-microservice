package cn.edu.ecommerce.auth.model;

import java.time.Instant;

public record UserAccount(
        Long id,
        String email,
        String nickname,
        String passwordHash,
        String role,
        String status,
        Instant createdAt
) {
}
