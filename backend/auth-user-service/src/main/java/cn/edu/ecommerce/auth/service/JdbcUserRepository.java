package cn.edu.ecommerce.auth.service;

import cn.edu.ecommerce.auth.model.UserAccount;
import cn.edu.ecommerce.common.IdGenerator;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JdbcUserRepository implements UserRepository {
    private final JdbcClient jdbcClient;
    private final IdGenerator idGenerator = new IdGenerator(System.currentTimeMillis() / 1000);

    public JdbcUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public UserAccount saveNew(String email, String nickname, String passwordHash, String role) {
        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("email already registered");
        }
        long id = idGenerator.nextId();
        jdbcClient.sql("""
                        insert into user_account (id, email, nickname, password_hash, role_code)
                        values (?, ?, ?, ?, ?)
                        """)
                .params(id, email, nickname, passwordHash, role)
                .update();
        return findByEmail(email).orElseThrow();
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return jdbcClient.sql("""
                        select id, email, nickname, password_hash, role_code, create_time
                        from user_account
                        where email = ? and deleted = 0
                        """)
                .param(email)
                .query(this::mapAccount)
                .optional();
    }

    @Override
    public List<UserAccount> findAll() {
        return jdbcClient.sql("""
                        select id, email, nickname, password_hash, role_code, create_time
                        from user_account
                        where deleted = 0
                        order by id
                        """)
                .query(this::mapAccount)
                .list();
    }

    private UserAccount mapAccount(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Timestamp timestamp = rs.getTimestamp("create_time");
        Instant createdAt = timestamp == null ? Instant.now() : timestamp.toInstant();
        return new UserAccount(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("nickname"),
                rs.getString("password_hash"),
                rs.getString("role_code"),
                createdAt
        );
    }
}
