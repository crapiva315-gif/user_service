package dev.alexeev.user_service.service;

import dev.alexeev.user_service.dto.user.*;
import dev.alexeev.user_service.entity.User;
import dev.alexeev.user_service.exception.DuplicateEmailException;
import dev.alexeev.user_service.exception.UserNotFoundException;
import dev.alexeev.user_service.mapper.UserMapper;
import dev.alexeev.user_service.repository.UserRepository;
import dev.alexeev.user_service.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

  private static final String USER_WITH_CARDS_CACHE = "userWithCards";

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public UserResponseDto getById(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    return userMapper.toDto(user);
  }

  @Transactional(readOnly = true)
  public List<UserResponseDto> getByIds(List<Long> ids) {
    return userMapper.toDtoList(userRepository.findAllById(ids));
  }

  @Cacheable(value = USER_WITH_CARDS_CACHE, key = "#id")
  @Transactional(readOnly = true)
  public UserWithCardsResponseDto getByIdWithCards(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    return userMapper.toDtoWithCards(user);
  }

  @Transactional(readOnly = true)
  public Page<UserResponseDto> getAll(String name, String surname, Pageable pageable) {
    var spec = UserSpecification.withFilters(name, surname);
    return userRepository.findAll(spec, pageable)
            .map(userMapper::toDto);
  }

  @Transactional
  public UserResponseDto create(UserCreateRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new DuplicateEmailException(request.getEmail());
    }
    User user = userMapper.toEntity(request);
    return userMapper.toDto(userRepository.save(user));
  }

  @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#id")
  @Transactional
  public UserResponseDto update(Long id, UserUpdateRequest request) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    userMapper.updateEntityFromDto(request, user);
    return userMapper.toDto(user);
  }

  @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#id")
  @Transactional
  public void activate(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    user.setActive(true);
  }

  @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#id")
  @Transactional
  public void deactivate(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    user.setActive(false);
  }

  @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#id")
  @Transactional
  public void delete(Long id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }
    userRepository.deleteById(id);
  }
}