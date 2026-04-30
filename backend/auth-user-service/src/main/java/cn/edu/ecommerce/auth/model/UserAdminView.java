package cn.edu.ecommerce.auth.model;

import java.time.Instant;

public record UserAdminView(Long id, String email, String nickname, String role, String status, Instant createdAt) {
    public static UserAdminView from(UserAccount account) {
        return new UserAdminView(account.id(), account.email(), account.nickname(), account.role(), account.status(), account.createdAt());
    }
}
