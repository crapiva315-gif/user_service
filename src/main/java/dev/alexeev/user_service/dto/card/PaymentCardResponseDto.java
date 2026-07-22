package dev.alexeev.user_service.dto.card;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentCardResponseDto {
  private Long id;
  private Long userId;
  private String number;
  private String holder;
  private LocalDate expirationDate;
  private boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}