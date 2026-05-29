package com.br.lvs_group.label_cat.controller;

import com.br.lvs_group.label_cat.dto.TypeOfLabelRequest;
import com.br.lvs_group.label_cat.dto.TypeOfLabelResponse;
import com.br.lvs_group.label_cat.service.TypeOfLabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types-of-labels")
@RequiredArgsConstructor
public class TypeOfLabelController {

    private final TypeOfLabelService service;

    @PostMapping
    public ResponseEntity<TypeOfLabelResponse> create(@Valid @RequestBody TypeOfLabelRequest request) {
        TypeOfLabelResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeOfLabelResponse> findById(@PathVariable Long id) {
        TypeOfLabelResponse response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TypeOfLabelResponse>> findAll() {
        List<TypeOfLabelResponse> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeOfLabelResponse> update(@PathVariable Long id, @Valid @RequestBody TypeOfLabelRequest request) {
        TypeOfLabelResponse response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
