package com.br.lvs_group.label_cat.dto;

import com.br.lvs_group.label_cat.entities.UserFunction;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private UserFunction function;
}
