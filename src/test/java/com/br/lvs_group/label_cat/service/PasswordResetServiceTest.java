package com.br.lvs_group.label_cat.service;

import com.br.lvs_group.label_cat.entities.PasswordResetToken;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.repositories.PasswordResetTokenRepository;
import com.br.lvs_group.label_cat.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    private PasswordEncoder passwordEncoder;

    private PasswordResetService passwordResetService;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        passwordResetService = new PasswordResetService(
                userRepository, tokenRepository, emailService, passwordEncoder
        );
    }

    @Test
    void shouldGenerateTokenAndSendEmailWhenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setName("Test User");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        passwordResetService.forgotPassword("user@example.com");

        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getUser().getId()).isEqualTo(1L);
        assertThat(savedToken.getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(savedToken.getExpiryDate()).isBefore(LocalDateTime.now().plusHours(2));
        assertThat(savedToken.isUsed()).isFalse();
        assertThat(savedToken.getToken()).startsWith("$2a$");

        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetToken(eq("user@example.com"), rawTokenCaptor.capture());
        String rawToken = rawTokenCaptor.getValue();

        assertThat(rawToken).matches("\\d{6}");
        assertThat(passwordEncoder.matches(rawToken, savedToken.getToken())).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.forgotPassword("unknown@example.com"))
                .isInstanceOf(RuntimeException.class);

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetToken(anyString(), anyString());
    }

    @Test
    void shouldVerifyTokenSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        String rawToken = "123456";
        String hashedToken = passwordEncoder.encode(rawToken);
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(hashedToken);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(eq(user), any(LocalDateTime.class)))
                .thenReturn(Optional.of(token));

        passwordResetService.verifyToken("user@example.com", rawToken);
    }

    @Test
    void shouldThrowExceptionWhenVerifyTokenExpired() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(eq(user), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.verifyToken("user@example.com", "123456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenVerifyTokenInvalid() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        String hashedToken = passwordEncoder.encode("654321");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(hashedToken);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(eq(user), any(LocalDateTime.class)))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.verifyToken("user@example.com", "123456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenVerifyTokenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.verifyToken("unknown@example.com", "123456"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("old-hashed-password");
        user.setFirstAccess(true);

        String rawToken = "123456";
        String hashedToken = passwordEncoder.encode(rawToken);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(hashedToken);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(eq(user), any(LocalDateTime.class)))
                .thenReturn(Optional.of(resetToken));

        passwordResetService.resetPassword("user@example.com", rawToken, "NewPassword123!", "NewPassword123!");

        assertThat(passwordEncoder.matches("NewPassword123!", user.getPassword())).isTrue();
        assertThat(user.getFirstAccess()).isFalse();
        assertThat(resetToken.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(resetToken);
    }

    @Test
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
        assertThatThrownBy(() ->
                passwordResetService.resetPassword("user@example.com", "123456", "NewPassword123!", "DifferentPassword!"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenResetPasswordTokenExpired() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(eq(user), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                passwordResetService.resetPassword("user@example.com", "123456", "NewPassword123!", "NewPassword123!"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenResetPasswordTokenInvalid() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        String hashedToken = passwordEncoder.encode("654321");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(hashedToken);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserAndUsedFalseAndExpiryDateAfterOrderByCreatedAtDesc(eq(user), any(LocalDateTime.class)))
                .thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() ->
                passwordResetService.resetPassword("user@example.com", "123456", "NewPassword123!", "NewPassword123!"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenResetPasswordUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                passwordResetService.resetPassword("unknown@example.com", "123456", "NewPassword123!", "NewPassword123!"))
                .isInstanceOf(RuntimeException.class);

        verify(tokenRepository, never()).save(any());
    }
}
