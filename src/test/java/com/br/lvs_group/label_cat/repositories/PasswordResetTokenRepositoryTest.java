package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.PasswordResetToken;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.entities.UserFunction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PasswordResetTokenRepository repository;

    private User createUser() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setFunction(UserFunction.ADMIN);
        return entityManager.persist(user);
    }

    private PasswordResetToken createToken(User user, String tokenValue, LocalDateTime expiryDate, boolean used) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(expiryDate);
        token.setUsed(used);
        return entityManager.persist(token);
    }

    @Test
    void shouldPersistAndFindPasswordResetToken() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setFunction(UserFunction.ADMIN);
        entityManager.persist(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("$2a$10$hashedTokenValue");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        PasswordResetToken saved = repository.save(token);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getToken()).isEqualTo("$2a$10$hashedTokenValue");
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindActiveTokenByUser() {
        User user = createUser();
        PasswordResetToken validToken = createToken(
                user, "$2a$10$valid", LocalDateTime.now().plusHours(1), false
        );
        createToken(user, "$2a$10$expired", LocalDateTime.now().minusHours(1), false);
        createToken(user, "$2a$10$used", LocalDateTime.now().plusHours(1), true);

        entityManager.flush();

        Optional<PasswordResetToken> found = repository
                .findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(user, LocalDateTime.now());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(validToken.getId());
        assertThat(found.get().getToken()).isEqualTo("$2a$10$valid");
    }

    @Test
    void shouldReturnEmptyWhenTokenIsExpired() {
        User user = createUser();
        createToken(user, "$2a$10$expired", LocalDateTime.now().minusHours(1), false);

        entityManager.flush();

        Optional<PasswordResetToken> found = repository
                .findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(user, LocalDateTime.now());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTokenIsUsed() {
        User user = createUser();
        createToken(user, "$2a$10$used", LocalDateTime.now().plusHours(1), true);

        entityManager.flush();

        Optional<PasswordResetToken> found = repository
                .findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(user, LocalDateTime.now());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoTokenExists() {
        User user = createUser();

        Optional<PasswordResetToken> found = repository
                .findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(user, LocalDateTime.now());

        assertThat(found).isEmpty();
    }
}
