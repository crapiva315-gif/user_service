package dev.alexeev.user_service.service;

import dev.alexeev.user_service.dto.user.UserCreateRequest;
import dev.alexeev.user_service.dto.user.UserResponseDto;
import dev.alexeev.user_service.dto.user.UserUpdateRequest;
import dev.alexeev.user_service.entity.User;
import dev.alexeev.user_service.exception.DuplicateEmailException;
import dev.alexeev.user_service.exception.UserNotFoundException;
import dev.alexeev.user_service.mapper.UserMapper;
import dev.alexeev.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserService userService;

  private User user;
  private UserResponseDto userResponseDto;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setName("Alexey");
    user.setSurname("Petrov");
    user.setEmail("alexey@example.com");
    user.setBirthDate(LocalDate.of(1995, 3, 12));
    user.setActive(true);

    userResponseDto = new UserResponseDto();
    userResponseDto.setId(1L);
    userResponseDto.setName("Alexey");
    userResponseDto.setEmail("alexey@example.com");
  }

  @Test
  void getById_shouldReturnUser_whenUserExists() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userMapper.toDto(user)).thenReturn(userResponseDto);

    UserResponseDto result = userService.getById(1L);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getEmail()).isEqualTo("alexey@example.com");
    verify(userRepository).findById(1L);
  }

  @Test
  void getById_shouldThrowException_whenUserNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getById(999L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("999");
  }

  @Test
  void getAll_shouldReturnPageOfUsers() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

    when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable)))
            .thenReturn(userPage);
    when(userMapper.toDto(user)).thenReturn(userResponseDto);

    Page<UserResponseDto> result = userService.getAll(null, null, pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getEmail()).isEqualTo("alexey@example.com");
  }

  @Test
  void create_shouldSaveUser_whenEmailIsUnique() {
    UserCreateRequest request = new UserCreateRequest();
    request.setName("Alexey");
    request.setSurname("Petrov");
    request.setEmail("alexey@example.com");
    request.setBirthDate(LocalDate.of(1995, 3, 12));

    when(userRepository.findByEmail("alexey@example.com")).thenReturn(Optional.empty());
    when(userMapper.toEntity(request)).thenReturn(user);
    when(userRepository.save(user)).thenReturn(user);
    when(userMapper.toDto(user)).thenReturn(userResponseDto);

    UserResponseDto result = userService.create(request);

    assertThat(result.getEmail()).isEqualTo("alexey@example.com");
    verify(userRepository).save(user);
  }

  @Test
  void create_shouldThrowException_whenEmailAlreadyExists() {
    UserCreateRequest request = new UserCreateRequest();
    request.setEmail("alexey@example.com");

    when(userRepository.findByEmail("alexey@example.com")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("alexey@example.com");

    verify(userRepository, never()).save(any());
  }

  @Test
  void update_shouldUpdateUser_whenUserExists() {
    UserUpdateRequest request = new UserUpdateRequest();
    request.setName("Updated");
    request.setSurname("Petrov");
    request.setEmail("alexey@example.com");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userMapper.toDto(user)).thenReturn(userResponseDto);

    userService.update(1L, request);

    verify(userMapper).updateEntityFromDto(request, user);
  }

  @Test
  void update_shouldThrowException_whenUserNotFound() {
    UserUpdateRequest request = new UserUpdateRequest();
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(999L, request))
            .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void activate_shouldSetActiveTrue_whenUserExists() {
    user.setActive(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    userService.activate(1L);

    assertThat(user.isActive()).isTrue();
  }

  @Test
  void activate_shouldThrowException_whenUserNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.activate(999L))
            .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void deactivate_shouldSetActiveFalse_whenUserExists() {
    user.setActive(true);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    userService.deactivate(1L);

    assertThat(user.isActive()).isFalse();
  }

  @Test
  void deactivate_shouldThrowException_whenUserNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.deactivate(999L))
            .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void delete_shouldDeleteUser_whenUserExists() {
    when(userRepository.existsById(1L)).thenReturn(true);

    userService.delete(1L);

    verify(userRepository).deleteById(1L);
  }

  @Test
  void delete_shouldThrowException_whenUserNotFound() {
    when(userRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> userService.delete(999L))
            .isInstanceOf(UserNotFoundException.class);

    verify(userRepository, never()).deleteById(any());
  }
}