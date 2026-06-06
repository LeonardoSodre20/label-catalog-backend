package com.br.lvs_group.label_cat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyTokenRequest {

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Token is required")
    @Pattern(regexp = "\\d{6}", message = "Token must be a 6-digit number")
    private String token;
}
