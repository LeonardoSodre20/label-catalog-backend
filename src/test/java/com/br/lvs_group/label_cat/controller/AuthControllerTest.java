package com.br.lvs_group.label_cat.controller;

import com.br.lvs_group.label_cat.dto.ForgotPasswordRequest;
import com.br.lvs_group.label_cat.dto.ResetPasswordRequest;
import com.br.lvs_group.label_cat.dto.VerifyTokenRequest;
import com.br.lvs_group.label_cat.security.JwtUtil;
import com.br.lvs_group.label_cat.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @Test
    void shouldReturnOkWhenForgotPasswordValid() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@example.com");

        doNothing().when(passwordResetService).forgotPassword("user@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset token sent to email"));
    }

    @Test
    void shouldReturnBadRequestWhenForgotPasswordEmailBlank() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenVerifyTokenValid() throws Exception {
        VerifyTokenRequest request = new VerifyTokenRequest();
        request.setEmail("user@example.com");
        request.setToken("123456");

        doNothing().when(passwordResetService).verifyToken("user@example.com", "123456");

        mockMvc.perform(post("/api/auth/verify-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token is valid"));
    }

    @Test
    void shouldReturnBadRequestWhenVerifyTokenInvalid() throws Exception {
        VerifyTokenRequest request = new VerifyTokenRequest();
        request.setEmail("user@example.com");
        request.setToken("123456");

        doThrow(new IllegalArgumentException("Token is invalid or expired"))
                .when(passwordResetService).verifyToken("user@example.com", "123456");

        mockMvc.perform(post("/api/auth/verify-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenResetPasswordValid() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setToken("123456");
        request.setPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        doNothing().when(passwordResetService)
                .resetPassword("user@example.com", "123456", "NewPassword123!", "NewPassword123!");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void shouldReturnBadRequestWhenResetPasswordInvalid() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setToken("123456");
        request.setPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        doThrow(new IllegalArgumentException("Token is invalid or expired"))
                .when(passwordResetService)
                .resetPassword("user@example.com", "123456", "NewPassword123!", "NewPassword123!");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
