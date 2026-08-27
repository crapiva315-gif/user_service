package dev.alexeev.user_service.integration;

import dev.alexeev.user_service.entity.PaymentCard;
import dev.alexeev.user_service.entity.User;
import dev.alexeev.user_service.repository.PaymentCardRepository;
import dev.alexeev.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import dev.alexeev.user_service.config.JpaAuditingConfig;
import org.springframework.context.annotation.Import;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(JpaAuditingConfig.class)
class RepositoryQueryIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  private static final AtomicLong ID_SEQUENCE = new AtomicLong(1);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PaymentCardRepository paymentCardRepository;

  @Test
  void findActiveUsersByEmailContaining_shouldReturnOnlyMatchingActiveUsers() {
    User active = newUser("Ivan", "Petrov", "ivan.petrov@example.com", true);
    User inactiveSameDomain = newUser("Petr", "Sidorov", "petr.sidorov@example.com", false);
    User activeDifferentDomain = newUser("Anna", "Ivanova", "anna@other.com", true);

    userRepository.saveAll(List.of(active, inactiveSameDomain, activeDifferentDomain));

    List<User> result = userRepository.findActiveUsersByEmailContaining("example.com");

    assertThat(result)
            .extracting(User::getEmail)
            .containsExactly("ivan.petrov@example.com");
  }

  @Test
  void countActiveUsers_shouldCountOnlyActiveOnes() {
    userRepository.saveAll(List.of(
            newUser("A", "A", "a@example.com", true),
            newUser("B", "B", "b@example.com", true),
            newUser("C", "C", "c@example.com", false)
    ));

    long count = userRepository.countActiveUsers();

    assertThat(count).isEqualTo(2);
  }

  @Test
  void findActiveCardsExpiringBefore_shouldReturnOnlyMatchingCards() {
    User user = userRepository.save(newUser("Card", "Owner", "card.owner@example.com", true));

    PaymentCard expiringSoon = newCard(user, "4111111111111111", LocalDate.of(2026, 9, 1), true);
    PaymentCard expiringLater = newCard(user, "4111111111111112", LocalDate.of(2030, 1, 1), true);
    PaymentCard inactiveButExpiringSoon = newCard(user, "4111111111111113", LocalDate.of(2026, 9, 1), false);

    paymentCardRepository.saveAll(List.of(expiringSoon, expiringLater, inactiveButExpiringSoon));

    List<PaymentCard> result = paymentCardRepository.findActiveCardsExpiringBefore(
            user.getId(), LocalDate.of(2027, 1, 1));

    assertThat(result)
            .extracting(PaymentCard::getNumber)
            .containsExactly("4111111111111111");
  }

  @Test
  void countActiveCards_shouldCountOnlyActiveOnes() {
    User user = userRepository.save(newUser("Card", "Counter", "card.counter@example.com", true));

    paymentCardRepository.saveAll(List.of(
            newCard(user, "4111111111111121", LocalDate.of(2028, 1, 1), true),
            newCard(user, "4111111111111122", LocalDate.of(2028, 1, 1), true),
            newCard(user, "4111111111111123", LocalDate.of(2028, 1, 1), false)
    ));

    long count = paymentCardRepository.countActiveCards();

    assertThat(count).isEqualTo(2);
  }

  private User newUser(String name, String surname, String email, boolean active) {
    User user = new User();
    user.setId(ID_SEQUENCE.getAndIncrement());
    user.setName(name);
    user.setSurname(surname);
    user.setEmail(email);
    user.setBirthDate(LocalDate.of(1990, 1, 1));
    user.setActive(active);
    return user;
  }

  private PaymentCard newCard(User user, String number, LocalDate expirationDate, boolean active) {
    PaymentCard card = new PaymentCard();
    card.setUser(user);
    card.setNumber(number);
    card.setHolder("TEST HOLDER");
    card.setExpirationDate(expirationDate);
    card.setActive(active);
    return card;
  }
}