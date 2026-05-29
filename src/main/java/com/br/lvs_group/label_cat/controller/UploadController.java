package com.br.lvs_group.label_cat.controller;

import com.br.lvs_group.label_cat.dto.UploadResponse;
import com.br.lvs_group.label_cat.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        UploadResponse response = uploadService.store(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UploadResponse> findById(@PathVariable Long id) {
        UploadResponse response = uploadService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> serveFile(@PathVariable Long id) {
        UploadResponse meta = uploadService.findById(id);
        Resource resource = uploadService.loadAsResource(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + meta.getFileName() + "\"")
                .body(resource);
    }
}
