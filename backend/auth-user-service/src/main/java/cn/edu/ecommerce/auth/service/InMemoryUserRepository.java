package cn.edu.ecommerce.auth.service;

import cn.edu.ecommerce.auth.model.UserAccount;
import cn.edu.ecommerce.common.IdGenerator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
    private final IdGenerator idGenerator = new IdGenerator(1);
    private final Map<Long, UserAccount> accountsById = new ConcurrentHashMap<>();
    private final Map<String, Long> idsByEmail = new ConcurrentHashMap<>();

    @Override
    public synchronized UserAccount saveNew(String email, String nickname, String passwordHash, String role, String status) {
        if (idsByEmail.containsKey(email)) {
            throw new IllegalArgumentException("email already registered");
        }
        long id = idGenerator.nextId();
        UserAccount account = new UserAccount(id, email, nickname, passwordHash, role, status, java.time.Instant.now());
        accountsById.put(id, account);
        idsByEmail.put(email, id);
        return account;
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        return Optional.ofNullable(accountsById.get(id));
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        Long id = idsByEmail.get(email);
        return id == null ? Optional.empty() : Optional.ofNullable(accountsById.get(id));
    }

    @Override
    public List<UserAccount> findAll() {
        return accountsById.values().stream()
                .sorted(Comparator.comparing(UserAccount::id))
                .toList();
    }

    @Override
    public synchronized UserAccount updateUser(Long id, String nickname, String role, String status) {
        UserAccount current = findById(id).orElseThrow(() -> new IllegalArgumentException("user not found"));
        UserAccount updated = new UserAccount(
                current.id(),
                current.email(),
                nickname,
                current.passwordHash(),
                role,
                status,
                current.createdAt()
        );
        accountsById.put(id, updated);
        return updated;
    }

    @Override
    public synchronized void updatePassword(Long id, String passwordHash) {
        UserAccount current = findById(id).orElseThrow(() -> new IllegalArgumentException("user not found"));
        accountsById.put(id, new UserAccount(
                current.id(),
                current.email(),
                current.nickname(),
                passwordHash,
                current.role(),
                current.status(),
                current.createdAt()
        ));
    }
}
