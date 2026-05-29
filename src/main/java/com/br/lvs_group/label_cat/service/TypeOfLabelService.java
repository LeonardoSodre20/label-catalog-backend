package com.br.lvs_group.label_cat.service;

import com.br.lvs_group.label_cat.dto.TypeOfLabelRequest;
import com.br.lvs_group.label_cat.dto.TypeOfLabelResponse;
import com.br.lvs_group.label_cat.entities.TypeOfLabel;
import com.br.lvs_group.label_cat.exception.ResourceNotFoundException;
import com.br.lvs_group.label_cat.repositories.TypeOfLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeOfLabelService {

    private final TypeOfLabelRepository repository;

    @Transactional
    public TypeOfLabelResponse create(TypeOfLabelRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Type name already exists: " + request.getName());
        }

        TypeOfLabel type = new TypeOfLabel();
        type.setName(request.getName());
        type.setDescription(request.getDescription());

        type = repository.save(type);
        return toResponse(type);
    }

    @Transactional(readOnly = true)
    public TypeOfLabelResponse findById(Long id) {
        TypeOfLabel type = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TypeOfLabel not found with id: " + id));
        return toResponse(type);
    }

    @Transactional(readOnly = true)
    public List<TypeOfLabelResponse> findAll() {
        return repository.findAll().stream()
                .map(TypeOfLabelService::toResponse)
                .toList();
    }

    @Transactional
    public TypeOfLabelResponse update(Long id, TypeOfLabelRequest request) {
        TypeOfLabel type = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TypeOfLabel not found with id: " + id));

        if (!type.getName().equals(request.getName()) && repository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Type name already exists: " + request.getName());
        }

        type.setName(request.getName());
        type.setDescription(request.getDescription());

        type = repository.save(type);
        return toResponse(type);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TypeOfLabel not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private static TypeOfLabelResponse toResponse(TypeOfLabel type) {
        return TypeOfLabelResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .description(type.getDescription())
                .createdAt(type.getCreatedAt())
                .updatedAt(type.getUpdatedAt())
                .build();
    }
}
