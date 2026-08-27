package dev.alexeev.user_service.security;

import dev.alexeev.user_service.exception.ForbiddenAccessException;
import org.springframework.stereotype.Component;

@Component
public class AccessGuard {

  private static final String ADMIN_ROLE = "ADMIN";

  public void requireSelfOrAdmin(Long targetUserId, Long callerUserId, String callerRole) {
    if (isAdmin(callerRole)) {
      return;
    }
    if (callerUserId == null || !callerUserId.equals(targetUserId)) {
      throw new ForbiddenAccessException("Not allowed to access this resource");
    }
  }

  public void requireAdmin(String callerRole) {
    if (!isAdmin(callerRole)) {
      throw new ForbiddenAccessException("Admin role required");
    }
  }

  private boolean isAdmin(String callerRole) {
    return ADMIN_ROLE.equalsIgnoreCase(callerRole);
  }
}