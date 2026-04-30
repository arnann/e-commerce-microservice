package cn.edu.ecommerce.auth.model;

public record AdminCreateUserRequest(String email, String nickname, String password, String role, String status) {
}
