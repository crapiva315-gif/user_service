package dev.alexeev.user_service.dto.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class UserCreateRequest {

  @NotNull(message = "User ID is required")
  private Long id;

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must not exceed 100 characters")
  private String name;

  @NotBlank(message = "Surname is required")
  @Size(max = 100, message = "Surname must not exceed 100 characters")
  private String surname;

  @NotNull(message = "Birth date is required")
  @Past(message = "Birth date must be in the past")
  private LocalDate birthDate;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;
}