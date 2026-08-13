package dev.alexeev.user_service.repository;

import dev.alexeev.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.active = true AND LOWER(u.email) LIKE LOWER(CONCAT('%', :emailPart, '%'))")
  List<User> findActiveUsersByEmailContaining(@Param("emailPart") String emailPart);

  @Query(value = "SELECT COUNT(*) FROM users WHERE active = true", nativeQuery = true)
  long countActiveUsers();
}
