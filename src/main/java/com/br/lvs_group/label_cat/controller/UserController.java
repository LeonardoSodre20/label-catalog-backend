package com.br.lvs_group.label_cat.controller;

import com.br.lvs_group.label_cat.dto.BatchDeleteRequest;
import com.br.lvs_group.label_cat.dto.UserRequest;
import com.br.lvs_group.label_cat.dto.UserResponse;
import com.br.lvs_group.label_cat.dto.UserUpdateRequest;
import com.br.lvs_group.label_cat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        UserResponse response = userService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String function,
            @RequestParam(required = false) String search,
            @PageableDefault Pageable pageable) {
        Page<UserResponse> response = userService.findAll(name, email, function, search, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@Valid @RequestBody BatchDeleteRequest request) {
        userService.batchDelete(request.getIds());
        return ResponseEntity.noContent().build();
    }
}
