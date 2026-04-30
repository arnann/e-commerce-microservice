package cn.edu.ecommerce.auth.service;

import cn.edu.ecommerce.auth.model.LoginResult;
import cn.edu.ecommerce.auth.model.TokenPrincipal;
import cn.edu.ecommerce.auth.model.UserAccount;
import cn.edu.ecommerce.auth.model.UserAdminView;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAccountService {
    private final UserRepository repository;
    private final JwtTokenService tokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserAccountService(UserRepository repository, JwtTokenService tokenService) {
        this.repository = repository;
        this.tokenService = tokenService;
    }

    public UserAccount register(String email, String nickname, String password) {
        validateCredentials(email, password);
        String displayName = nickname == null || nickname.isBlank() ? email : nickname.trim();
        return repository.saveNew(email.trim().toLowerCase(), displayName, passwordEncoder.encode(password), "CUSTOMER", "NORMAL");
    }

    public LoginResult registerAndLogin(String email, String nickname, String password) {
        UserAccount account = register(email, nickname, password);
        return new LoginResult(account.id(), account.email(), account.nickname(), account.role(), tokenService.issue(account));
    }

    public LoginResult login(String email, String password) {
        UserAccount account = repository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("invalid email or password"));
        if (!"NORMAL".equals(account.status())) {
            throw new IllegalArgumentException("account disabled");
        }
        if (!passwordEncoder.matches(password, account.passwordHash())) {
            throw new IllegalArgumentException("invalid email or password");
        }
        return new LoginResult(account.id(), account.email(), account.nickname(), account.role(), tokenService.issue(account));
    }

    public TokenPrincipal parseToken(String token) {
        return tokenService.parse(token);
    }

    public List<UserAdminView> listUsers() {
        return repository.findAll().stream()
                .map(UserAdminView::from)
                .toList();
    }

    public UserAdminView createUser(String email, String nickname, String password, String role, String status) {
        validateCredentials(email, password);
        String normalizedRole = normalizeRole(role);
        String normalizedStatus = normalizeStatus(status);
        String displayName = nickname == null || nickname.isBlank() ? email : nickname.trim();
        UserAccount account = repository.saveNew(
                email.trim().toLowerCase(),
                displayName,
                passwordEncoder.encode(password),
                normalizedRole,
                normalizedStatus
        );
        return UserAdminView.from(account);
    }

    public UserAdminView updateUser(Long id, String nickname, String role, String status) {
        UserAccount current = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        String displayName = nickname == null || nickname.isBlank() ? current.nickname() : nickname.trim();
        return UserAdminView.from(repository.updateUser(id, displayName, normalizeRole(role), normalizeStatus(status)));
    }

    public UserAdminView resetPassword(Long id, String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }
        repository.updatePassword(id, passwordEncoder.encode(password));
        return repository.findById(id)
                .map(UserAdminView::from)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    private void validateCredentials(String email, String password) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("valid email required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }
    }

    private String normalizeRole(String role) {
        String normalized = role == null || role.isBlank() ? "CUSTOMER" : role.trim().toUpperCase();
        if (!normalized.equals("CUSTOMER") && !normalized.equals("ADMIN")) {
            throw new IllegalArgumentException("invalid role");
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        String normalized = status == null || status.isBlank() ? "NORMAL" : status.trim().toUpperCase();
        if (!normalized.equals("NORMAL") && !normalized.equals("DISABLED")) {
            throw new IllegalArgumentException("invalid status");
        }
        return normalized;
    }
}
