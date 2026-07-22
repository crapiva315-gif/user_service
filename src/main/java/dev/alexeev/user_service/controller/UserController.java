package dev.alexeev.user_service.controller;

import dev.alexeev.user_service.dto.user.UserCreateRequest;
import dev.alexeev.user_service.dto.user.UserResponseDto;
import dev.alexeev.user_service.dto.user.UserUpdateRequest;
import dev.alexeev.user_service.dto.user.UserWithCardsResponseDto;
import dev.alexeev.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getById(id));
  }

  @GetMapping("/{id}/full")
  public ResponseEntity<UserWithCardsResponseDto> getByIdWithCards(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getByIdWithCards(id));
  }

  @GetMapping
  public ResponseEntity<List<UserResponseDto>> getAll() {
    return ResponseEntity.ok(userService.getAll());
  }

  @PostMapping
  public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserCreateRequest request) {
    UserResponseDto created = userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDto> update(@PathVariable Long id,
                                                @Valid @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(userService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}