package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.PasswordResetToken;
import com.br.lvs_group.label_cat.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(User user, LocalDateTime now);
}
