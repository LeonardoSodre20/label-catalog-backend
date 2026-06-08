package com.br.lvs_group.label_cat.service;

import com.br.lvs_group.label_cat.dto.UserRequest;
import com.br.lvs_group.label_cat.dto.UserResponse;
import com.br.lvs_group.label_cat.dto.UserUpdateRequest;
import com.br.lvs_group.label_cat.entities.User;
import com.br.lvs_group.label_cat.entities.UserFunction;
import com.br.lvs_group.label_cat.exception.ResourceNotFoundException;
import com.br.lvs_group.label_cat.repositories.LabelRepository;
import com.br.lvs_group.label_cat.repositories.UserRepository;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void shouldCreateUserWithGeneratedPassword() {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setFunction(UserFunction.ADMIN);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getFunction()).isEqualTo(UserFunction.ADMIN);
        assertThat(response.getFirstAccess()).isTrue();

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getFirstAccess()).isTrue();

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendGeneratedPassword(eq("john@example.com"), passwordCaptor.capture());
        assertThat(passwordCaptor.getValue()).hasSize(8);
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithDuplicateEmail() {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendGeneratedPassword(anyString(), anyString());
    }

    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setFirstAccess(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstAccess()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateUserWithoutChangingPasswordOrEmail() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("John Updated");
        request.setFunction(UserFunction.ADMIN);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setPassword("old-encoded-password");
        existingUser.setFirstAccess(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.update(1L, request);

        assertThat(response.getName()).isEqualTo("John Updated");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getFunction()).isEqualTo(UserFunction.ADMIN);
        assertThat(response.getFirstAccess()).isFalse();

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("old-encoded-password");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldBatchDeleteUsers() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(user1, user2));

        userService.batchDelete(List.of(1L, 2L));

        verify(labelRepository).nullifyCreatedBy(List.of(1L, 2L));
        verify(userRepository).deleteAllByIdInBatch(List.of(1L, 2L));
    }

    @Test
    void shouldThrowExceptionWhenBatchDeleteNonExistentUser() {
        User user1 = new User();
        user1.setId(1L);

        when(userRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(user1));

        assertThatThrownBy(() -> userService.batchDelete(List.of(1L, 99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(labelRepository, never()).nullifyCreatedBy(anyList());
        verify(userRepository, never()).deleteAllByIdInBatch(anyList());
    }
}
