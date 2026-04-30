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
    public synchronized UserAccount saveNew(String email, String nickname, String passwordHash, String role) {
        if (idsByEmail.containsKey(email)) {
            throw new IllegalArgumentException("email already registered");
        }
        long id = idGenerator.nextId();
        UserAccount account = new UserAccount(id, email, nickname, passwordHash, role, java.time.Instant.now());
        accountsById.put(id, account);
        idsByEmail.put(email, id);
        return account;
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
}
