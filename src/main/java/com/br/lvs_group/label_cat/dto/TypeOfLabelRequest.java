package com.br.lvs_group.label_cat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TypeOfLabelRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
