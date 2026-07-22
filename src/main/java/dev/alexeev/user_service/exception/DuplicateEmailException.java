package dev.alexeev.user_service.exception;

public class DuplicateEmailException extends RuntimeException {
  public DuplicateEmailException(String email) {
    super("User with email=" + email + " already exists");
  }
}