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
        return repository.saveNew(email.trim().toLowerCase(), displayName, passwordEncoder.encode(password), "CUSTOMER");
    }

    public LoginResult login(String email, String password) {
        UserAccount account = repository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("invalid email or password"));
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

    private void validateCredentials(String email, String password) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("valid email required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }
    }
}
