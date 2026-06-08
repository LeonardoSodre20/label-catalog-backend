package com.br.lvs_group.label_cat.dto;

import com.br.lvs_group.label_cat.entities.UserFunction;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type;
    private String email;
    private UserFunction function;
    private Boolean firstAccess;
}
