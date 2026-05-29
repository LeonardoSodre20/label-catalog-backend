package com.br.lvs_group.label_cat.service;

import com.br.lvs_group.label_cat.dto.UploadResponse;
import com.br.lvs_group.label_cat.entities.Upload;
import com.br.lvs_group.label_cat.exception.ResourceNotFoundException;
import com.br.lvs_group.label_cat.repositories.UploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final UploadRepository uploadRepository;

    @Value("${upload.directory:uploads}")
    private String uploadDir;

    @Transactional
    public UploadResponse store(MultipartFile file) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + extension;
            Path filePath = dir.resolve(storedName);

            Files.copy(file.getInputStream(), filePath);

            Upload upload = new Upload();
            upload.setFileName(originalName);
            upload.setFilePath(filePath.toString());
            upload.setContentType(file.getContentType());
            upload.setSize(file.getSize());
            upload.setCreatedAt(LocalDateTime.now());

            upload = uploadRepository.save(upload);
            return toResponse(upload);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Transactional(readOnly = true)
    public UploadResponse findById(Long id) {
        Upload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Upload not found with id: " + id));
        return toResponse(upload);
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(Long id) {
        Upload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Upload not found with id: " + id));

        try {
            Path file = Paths.get(upload.getFilePath());
            InputStream is = Files.newInputStream(file);
            return new InputStreamResource(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file", e);
        }
    }

    private UploadResponse toResponse(Upload upload) {
        return UploadResponse.builder()
                .id(upload.getId())
                .fileName(upload.getFileName())
                .contentType(upload.getContentType())
                .size(upload.getSize())
                .url("/api/uploads/" + upload.getId() + "/file")
                .createdAt(upload.getCreatedAt())
                .build();
    }
}
