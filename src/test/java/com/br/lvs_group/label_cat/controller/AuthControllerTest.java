package com.br.lvs_group.label_cat.controller;

import com.br.lvs_group.label_cat.dto.AuthRequest;
import com.br.lvs_group.label_cat.dto.ForgotPasswordRequest;
import com.br.lvs_group.label_cat.dto.ResetPasswordRequest;
import com.br.lvs_group.label_cat.dto.VerifyTokenRequest;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.entities.UserFunction;
import com.br.lvs_group.label_cat.repositories.UserRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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

    @MockitoBean
    private UserRepository userRepository;

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
    void shouldLoginAndReturnFirstAccess() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFunction(UserFunction.ADMIN);
        user.setFirstAccess(true);

        Authentication auth = new UsernamePasswordAuthenticationToken("user@example.com", null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("user@example.com", "ADMIN")).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.firstAccess").value(true));
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
