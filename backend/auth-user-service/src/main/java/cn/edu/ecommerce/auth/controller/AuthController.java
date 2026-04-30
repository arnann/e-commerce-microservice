package cn.edu.ecommerce.auth.controller;

import cn.edu.ecommerce.auth.model.AdminCreateUserRequest;
import cn.edu.ecommerce.auth.model.LoginRequest;
import cn.edu.ecommerce.auth.model.RegisterRequest;
import cn.edu.ecommerce.auth.model.ResetPasswordRequest;
import cn.edu.ecommerce.auth.model.UpdateUserRequest;
import cn.edu.ecommerce.auth.service.UserAccountService;
import cn.edu.ecommerce.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        return ApiResponse.ok(userAccountService.registerAndLogin(request.email(), request.nickname(), request.password()));
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(userAccountService.login(request.email(), request.password()));
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("auth-user-service");
    }

    @GetMapping("/users")
    public ApiResponse<?> listUsers() {
        return ApiResponse.ok(userAccountService.listUsers());
    }

    @PostMapping("/users")
    public ApiResponse<?> createUser(@RequestBody AdminCreateUserRequest request) {
        return ApiResponse.ok(userAccountService.createUser(
                request.email(),
                request.nickname(),
                request.password(),
                request.role(),
                request.status()
        ));
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(userAccountService.updateUser(userId, request.nickname(), request.role(), request.status()));
    }

    @PostMapping("/users/{userId}/reset-password")
    public ApiResponse<?> resetPassword(@PathVariable Long userId, @RequestBody ResetPasswordRequest request) {
        return ApiResponse.ok(userAccountService.resetPassword(userId, request.password()));
    }
}
