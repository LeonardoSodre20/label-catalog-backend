package com.br.lvs_group.label_cat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class LabelResponse {

    private Long id;
    private String name;
    private String description;
    private String codeRef;
    private Long typeId;
    private String typeName;
    private Long createdById;
    private String createdByName;
    private Integer qtdByBatch;
    private Long imageId;
    private String imageUrl;
    private String localization;
    private Map<String, Object> fields;
    private String sector;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
