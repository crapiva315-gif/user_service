package dev.alexeev.user_service.exception;

public class MaxCardsLimitExceededException extends RuntimeException {
  public MaxCardsLimitExceededException(Long userId) {
    super("User with id=" + userId + " already has the maximum number of payment cards (5)");
  }
}