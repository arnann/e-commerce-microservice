package cn.edu.ecommerce.auth.model;

public record UpdateUserRequest(String nickname, String role, String status) {
}
