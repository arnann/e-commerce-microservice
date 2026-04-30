package cn.edu.ecommerce.auth;

import cn.edu.ecommerce.auth.model.LoginResult;
import cn.edu.ecommerce.auth.model.UserAdminView;
import cn.edu.ecommerce.auth.model.UserAccount;
import cn.edu.ecommerce.auth.service.InMemoryUserRepository;
import cn.edu.ecommerce.auth.service.JwtTokenService;
import cn.edu.ecommerce.auth.service.UserAccountService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountServiceTest {

    @Test
    void registersCustomerAndLogsInWithJwtRoleClaim() {
        UserAccountService service = new UserAccountService(
                new InMemoryUserRepository(),
                JwtTokenService.forTests()
        );

        UserAccount account = service.register("alice@example.com", "Alice", "s3cret123");
        LoginResult result = service.login("alice@example.com", "s3cret123");

        assertThat(account.role()).isEqualTo("CUSTOMER");
        assertThat(result.userId()).isEqualTo(account.id());
        assertThat(result.token()).isNotBlank();
        assertThat(service.parseToken(result.token()).role()).isEqualTo("CUSTOMER");
    }

    @Test
    void rejectsDuplicateEmail() {
        UserAccountService service = new UserAccountService(
                new InMemoryUserRepository(),
                JwtTokenService.forTests()
        );

        service.register("alice@example.com", "Alice", "s3cret123");

        assertThatThrownBy(() -> service.register("alice@example.com", "Alice Clone", "another123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email already registered");
    }

    @Test
    void adminCanCreateUpdateDisableAndResetUsers() {
        UserAccountService service = new UserAccountService(
                new InMemoryUserRepository(),
                JwtTokenService.forTests()
        );

        UserAdminView created = service.createUser("operator@example.com", "Operator", "starter123", "CUSTOMER", "NORMAL");
        UserAdminView updated = service.updateUser(created.id(), "Ops Admin", "ADMIN", "DISABLED");

        assertThat(created.email()).isEqualTo("operator@example.com");
        assertThat(updated.nickname()).isEqualTo("Ops Admin");
        assertThat(updated.role()).isEqualTo("ADMIN");
        assertThat(updated.status()).isEqualTo("DISABLED");
        assertThatThrownBy(() -> service.login("operator@example.com", "starter123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("account disabled");

        service.updateUser(created.id(), "Ops Admin", "ADMIN", "NORMAL");
        service.resetPassword(created.id(), "changed123");

        LoginResult login = service.login("operator@example.com", "changed123");
        assertThat(login.role()).isEqualTo("ADMIN");
    }
}
