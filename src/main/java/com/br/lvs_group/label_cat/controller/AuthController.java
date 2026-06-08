package com.br.lvs_group.label_cat.controller;

import com.br.lvs_group.label_cat.dto.AuthRequest;
import com.br.lvs_group.label_cat.dto.AuthResponse;
import com.br.lvs_group.label_cat.dto.ForgotPasswordRequest;
import com.br.lvs_group.label_cat.dto.ResetPasswordRequest;
import com.br.lvs_group.label_cat.dto.VerifyTokenRequest;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.entities.UserFunction;
import com.br.lvs_group.label_cat.repositories.UserRepository;
import com.br.lvs_group.label_cat.security.JwtUtil;
import com.br.lvs_group.label_cat.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Password reset token sent to email"));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, String>> verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        passwordResetService.verifyToken(request.getEmail(), request.getToken());
        return ResponseEntity.ok(Map.of("message", "Token is valid"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(
                request.getEmail(), request.getToken(),
                request.getPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserFunction function = user.getFunction() != null ? user.getFunction() : UserFunction.OPERATOR;
        String token = jwtUtil.generateToken(email, function.name());
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", email, function, user.getFirstAccess()));
    }
}
