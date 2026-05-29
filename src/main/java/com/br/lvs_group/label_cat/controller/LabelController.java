package com.br.lvs_group.label_cat.controller;

import com.br.lvs_group.label_cat.dto.LabelRequest;
import com.br.lvs_group.label_cat.dto.LabelResponse;
import com.br.lvs_group.label_cat.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelResponse> create(@Valid @RequestBody LabelRequest request) {
        LabelResponse response = labelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabelResponse> findById(@PathVariable Long id) {
        LabelResponse response = labelService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code-ref/{codeRef}")
    public ResponseEntity<LabelResponse> findByCodeRef(@PathVariable String codeRef) {
        LabelResponse response = labelService.findByCodeRef(codeRef);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<LabelResponse>> findAll(@PageableDefault Pageable pageable) {
        Page<LabelResponse> page = labelService.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabelResponse> update(@PathVariable Long id, @Valid @RequestBody LabelRequest request) {
        LabelResponse response = labelService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        labelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
