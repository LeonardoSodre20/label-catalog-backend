package com.br.lvs_group.label_cat.service;

import com.br.lvs_group.label_cat.entities.PasswordResetToken;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.exception.ResourceNotFoundException;
import com.br.lvs_group.label_cat.repositories.PasswordResetTokenRepository;
import com.br.lvs_group.label_cat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public void verifyToken(String email, String rawToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        PasswordResetToken resetToken = tokenRepository
                .findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(user, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Token is invalid or expired"));

        if (!passwordEncoder.matches(rawToken, resetToken.getToken())) {
            throw new IllegalArgumentException("Token is invalid or expired");
        }
    }

    @Transactional
    public void resetPassword(String email, String rawToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        PasswordResetToken resetToken = tokenRepository
                .findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(user, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Token is invalid or expired"));

        if (!passwordEncoder.matches(rawToken, resetToken.getToken())) {
            throw new IllegalArgumentException("Token is invalid or expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstAccess(false);
        resetToken.setUsed(true);

        userRepository.save(user);
        tokenRepository.save(resetToken);
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String rawToken = generateToken();
        String hashedToken = passwordEncoder.encode(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(hashedToken);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        emailService.sendPasswordResetToken(user.getEmail(), rawToken);
    }

    private String generateToken() {
        int code = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", code);
    }
}
