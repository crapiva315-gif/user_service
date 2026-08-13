package dev.alexeev.user_service.repository;

import dev.alexeev.user_service.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
  Page<PaymentCard> findByUserId(Long userId, Pageable pageable);

  long countByUserId(Long userId);
  @Query("SELECT c FROM PaymentCard c WHERE c.user.id = :userId AND c.expirationDate <= :beforeDate AND c.active = true")
  List<PaymentCard> findActiveCardsExpiringBefore(@Param("userId") Long userId,
                                                  @Param("beforeDate") LocalDate beforeDate);

  @Query(value = "SELECT COUNT(*) FROM payment_cards WHERE active = true", nativeQuery = true)
  long countActiveCards();
}
