package cn.edu.ecommerce.auth.service;

import cn.edu.ecommerce.auth.model.UserAccount;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    UserAccount saveNew(String email, String nickname, String passwordHash, String role, String status);

    Optional<UserAccount> findById(Long id);

    Optional<UserAccount> findByEmail(String email);

    List<UserAccount> findAll();

    UserAccount updateUser(Long id, String nickname, String role, String status);

    void updatePassword(Long id, String passwordHash);
}
