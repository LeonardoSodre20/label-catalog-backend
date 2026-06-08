package com.br.lvs_group.label_cat.service;

import com.br.lvs_group.label_cat.dto.UserRequest;
import com.br.lvs_group.label_cat.dto.UserResponse;
import com.br.lvs_group.label_cat.dto.UserUpdateRequest;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.exception.ResourceNotFoundException;
import com.br.lvs_group.label_cat.repositories.LabelRepository;
import com.br.lvs_group.label_cat.repositories.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        String rawPassword = generatePassword();
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFunction(request.getFunction());
        user.setFirstAccess(true);

        user = userRepository.save(user);
        emailService.sendGeneratedPassword(user.getEmail(), rawPassword);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(String name, String email, String function, String search, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (function != null && !function.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("function")), function.toLowerCase()));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("function")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable).map(UserService::toResponse);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setName(request.getName());
        user.setFunction(request.getFunction());

        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void batchDelete(List<Long> ids) {
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            List<Long> foundIds = users.stream().map(User::getId).toList();
            List<Long> notFound = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException("Users not found with ids: " + notFound);
        }

        labelRepository.nullifyCreatedBy(ids);
        userRepository.deleteAllByIdInBatch(ids);
    }

    private static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .function(user.getFunction())
                .firstAccess(user.getFirstAccess())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}
