package com.br.lvs_group.label_cat.controller;

import com.br.lvs_group.label_cat.dto.AuthRequest;
import com.br.lvs_group.label_cat.dto.AuthResponse;
import com.br.lvs_group.label_cat.security.JwtUtil;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String email = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        String function = role.startsWith("ROLE_") ? role.substring(5) : role;

        String token = jwtUtil.generateToken(email, function);
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", email, function));
    }
}
