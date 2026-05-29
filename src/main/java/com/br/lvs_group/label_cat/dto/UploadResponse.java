package com.br.lvs_group.label_cat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UploadResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private Long size;
    private String url;
    private LocalDateTime createdAt;
}
