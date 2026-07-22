package dev.alexeev.user_service.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDto {
  private Long id;
  private String name;
  private String surname;
  private LocalDate birthDate;
  private String email;
  private boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}