package com.br.lvs_group.label_cat.dto;

import com.br.lvs_group.label_cat.entities.UserFunction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private UserFunction function;
    private Boolean firstAccess;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
