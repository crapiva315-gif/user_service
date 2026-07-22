package dev.alexeev.user_service.repository;

import dev.alexeev.user_service.entity.PaymentCard;
import dev.alexeev.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
  List<PaymentCard> findByUserId(Long userId);
  Optional<User> findByEmail(String email);
}
