package com.br.lvs_group.label_cat.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteRequest {

    @NotEmpty(message = "At least one ID is required")
    private List<Long> ids;
}
