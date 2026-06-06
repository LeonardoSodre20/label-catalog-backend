package com.br.lvs_group.label_cat.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    void shouldAcceptValidForgotPasswordRequest() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@example.com");

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectForgotPasswordRequestWithBlankEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("");

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectForgotPasswordRequestWithInvalidEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("invalid-email");

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldAcceptValidVerifyTokenRequest() {
        VerifyTokenRequest request = new VerifyTokenRequest();
        request.setEmail("user@example.com");
        request.setToken("123456");

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectVerifyTokenRequestWithInvalidTokenFormat() {
        VerifyTokenRequest request = new VerifyTokenRequest();
        request.setEmail("user@example.com");
        request.setToken("abc123");

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectVerifyTokenRequestWithShortToken() {
        VerifyTokenRequest request = new VerifyTokenRequest();
        request.setEmail("user@example.com");
        request.setToken("12345");

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldAcceptValidResetPasswordRequest() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setToken("123456");
        request.setPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectResetPasswordRequestWithBlankPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setToken("123456");
        request.setPassword("");
        request.setConfirmPassword("NewPassword123!");

        var violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
