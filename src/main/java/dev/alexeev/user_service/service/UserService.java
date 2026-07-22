package dev.alexeev.user_service.service;

import dev.alexeev.user_service.dto.user.*;
import dev.alexeev.user_service.entity.User;
import dev.alexeev.user_service.mapper.UserMapper;
import dev.alexeev.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public UserResponseDto getById(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User with id=" + id + " not found"));
    return userMapper.toDto(user);
  }

  @Transactional(readOnly = true)
  public List<UserResponseDto> getAll() {
    return userMapper.toDtoList(userRepository.findAll());
  }

  @Transactional
  public UserResponseDto create(UserCreateRequest request) {
    User user = userMapper.toEntity(request);
    return userMapper.toDto(userRepository.save(user));
  }

  @Transactional
  public UserResponseDto update(Long id, UserUpdateRequest request) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User with id=" + id + " not found"));
    userMapper.updateEntityFromDto(request, user);
    return userMapper.toDto(user);
  }

  @Transactional
  public void delete(Long id) {
    if (!userRepository.existsById(id)) {
      throw new EntityNotFoundException("User with id=" + id + " not found");
    }
    userRepository.deleteById(id);
  }
}