package dev.alexeev.user_service.repository;

import dev.alexeev.user_service.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
  Page<PaymentCard> findByUserId(Long userId, Pageable pageable);

  long countByUserId(Long userId);
}
