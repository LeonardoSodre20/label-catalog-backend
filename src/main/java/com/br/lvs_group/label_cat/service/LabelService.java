package com.br.lvs_group.label_cat.service;

import com.br.lvs_group.label_cat.dto.LabelRequest;
import com.br.lvs_group.label_cat.dto.LabelResponse;
import com.br.lvs_group.label_cat.entities.Label;
import com.br.lvs_group.label_cat.entities.TypeOfLabel;
import com.br.lvs_group.label_cat.entities.Upload;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.exception.ResourceNotFoundException;
import com.br.lvs_group.label_cat.repositories.LabelRepository;
import com.br.lvs_group.label_cat.repositories.TypeOfLabelRepository;
import com.br.lvs_group.label_cat.repositories.UploadRepository;
import com.br.lvs_group.label_cat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final TypeOfLabelRepository typeOfLabelRepository;
    private final UserRepository userRepository;
    private final UploadRepository uploadRepository;

    @Transactional
    public LabelResponse create(LabelRequest request) {
        if (labelRepository.existsByCodeRef(request.getCodeRef())) {
            throw new IllegalArgumentException("Code ref already exists: " + request.getCodeRef());
        }

        TypeOfLabel type = typeOfLabelRepository.findByName(request.getTypeName())
                .orElseThrow(() -> new ResourceNotFoundException("TypeOfLabel not found with name: " + request.getTypeName()));

        Label label = new Label();
        label.setCreatedBy(getAuthenticatedUser());
        applyRequest(label, request, type);

        label = labelRepository.save(label);
        return toResponse(label);
    }

    @Transactional(readOnly = true)
    public LabelResponse findById(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));
        return toResponse(label);
    }

    @Transactional(readOnly = true)
    public LabelResponse findByCodeRef(String codeRef) {
        Label label = labelRepository.findByCodeRef(codeRef)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with codeRef: " + codeRef));
        return toResponse(label);
    }

    @Transactional(readOnly = true)
    public Page<LabelResponse> findAll(Pageable pageable) {
        return labelRepository.findAll(pageable).map(LabelService::toResponse);
    }

    @Transactional
    public LabelResponse update(Long id, LabelRequest request) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));

        if (!label.getCodeRef().equals(request.getCodeRef()) && labelRepository.existsByCodeRef(request.getCodeRef())) {
            throw new IllegalArgumentException("Code ref already exists: " + request.getCodeRef());
        }

        TypeOfLabel type = typeOfLabelRepository.findByName(request.getTypeName())
                .orElseThrow(() -> new ResourceNotFoundException("TypeOfLabel not found with name: " + request.getTypeName()));

        applyRequest(label, request, type);

        label = labelRepository.save(label);
        return toResponse(label);
    }

    @Transactional
    public void delete(Long id) {
        if (!labelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Label not found with id: " + id);
        }
        labelRepository.deleteById(id);
    }

    private static void applyRequest(Label label, LabelRequest request, TypeOfLabel type, UploadRepository uploadRepository) {
        label.setName(request.getName());
        label.setDescription(request.getDescription());
        label.setCodeRef(request.getCodeRef());
        label.setType(type);
        label.setQtdByBatch(request.getQtdByBatch());
        if (request.getImageId() != null) {
            Upload image = uploadRepository.findById(request.getImageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Upload not found with id: " + request.getImageId()));
            label.setImage(image);
        } else {
            label.setImage(null);
        }
        label.setLocalization(request.getLocalization());
        label.setFields(request.getFields());
        label.setSector(request.getSector());
    }

    private void applyRequest(Label label, LabelRequest request, TypeOfLabel type) {
        applyRequest(label, request, type, uploadRepository);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private static LabelResponse toResponse(Label label) {
        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .description(label.getDescription())
                .codeRef(label.getCodeRef())
                .typeId(label.getType().getId())
                .typeName(label.getType().getName())
                .createdById(label.getCreatedBy() != null ? label.getCreatedBy().getId() : null)
                .createdByName(label.getCreatedBy() != null ? label.getCreatedBy().getName() : null)
                .qtdByBatch(label.getQtdByBatch())
                .imageId(label.getImage() != null ? label.getImage().getId() : null)
                .imageUrl(label.getImage() != null ? "/api/uploads/" + label.getImage().getId() + "/file" : null)
                .localization(label.getLocalization())
                .fields(label.getFields())
                .sector(label.getSector())
                .createdAt(label.getCreatedAt())
                .updatedAt(label.getUpdatedAt())
                .build();
    }
}
