package cn.edu.ecommerce.auth.service;

import cn.edu.ecommerce.auth.model.TokenPrincipal;
import cn.edu.ecommerce.auth.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtTokenService {
    private final SecretKey key;
    private final Duration ttl;

    public JwtTokenService() {
        this("graduation-design-ecommerce-secret-2026", Duration.ofHours(8));
    }

    public JwtTokenService(String secret, Duration ttl) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = ttl;
    }

    public static JwtTokenService forTests() {
        return new JwtTokenService("test-secret-for-ecommerce-auth-service-2026", Duration.ofHours(1));
    }

    public String issue(UserAccount account) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + ttl.toMillis());
        return Jwts.builder()
                .subject(String.valueOf(account.id()))
                .claim("email", account.email())
                .claim("role", account.role())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(key)
                .compact();
    }

    public TokenPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new TokenPrincipal(
                Long.valueOf(claims.getSubject()),
                claims.get("email", String.class),
                claims.get("role", String.class)
        );
    }
}
