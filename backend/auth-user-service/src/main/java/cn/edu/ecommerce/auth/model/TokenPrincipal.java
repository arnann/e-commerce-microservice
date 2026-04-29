package cn.edu.ecommerce.auth.model;

public record TokenPrincipal(Long userId, String email, String role) {
}
