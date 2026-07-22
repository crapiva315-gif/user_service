package dev.alexeev.user_service.exception;

public class InactiveUserException extends RuntimeException {
  public InactiveUserException(Long userId) {
    super("Cannot add a payment card for inactive user with id=" + userId);
  }
}