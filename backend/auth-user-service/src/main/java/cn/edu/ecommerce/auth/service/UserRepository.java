package cn.edu.ecommerce.auth.service;

import cn.edu.ecommerce.auth.model.UserAccount;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    UserAccount saveNew(String email, String nickname, String passwordHash, String role);

    Optional<UserAccount> findByEmail(String email);

    List<UserAccount> findAll();
}
