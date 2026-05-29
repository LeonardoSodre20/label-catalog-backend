package com.br.lvs_group.label_cat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class LabelRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Code ref is required")
    private String codeRef;

    @NotBlank(message = "Type name is required")
    private String typeName;

    private Integer qtdByBatch;

    private Long imageId;

    private String localization;

    private Map<String, Object> fields;

    private String sector;
}
