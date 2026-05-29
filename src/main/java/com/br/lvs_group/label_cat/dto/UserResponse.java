package com.br.lvs_group.label_cat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String function;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
