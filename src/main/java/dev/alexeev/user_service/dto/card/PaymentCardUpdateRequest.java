package dev.alexeev.user_service.dto.card;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentCardUpdateRequest {

  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "\\d{16,19}", message = "Card number must contain 16 to 19 digits")
  private String number;

  @NotBlank(message = "Card holder name is required")
  @Size(max = 150, message = "Card holder name must not exceed 150 characters")
  private String holder;

  @NotNull(message = "Expiration date is required")
  @Future(message = "Expiration date must be in the future")
  private LocalDate expirationDate;
}