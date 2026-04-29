package cn.edu.ecommerce.auth.model;

public record LoginResult(Long userId, String email, String nickname, String role, String token) {
}
