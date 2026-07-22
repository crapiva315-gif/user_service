package dev.alexeev.user_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "payment_cards")
public class PaymentCard extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String number;

  @Column(nullable = false)
  private String holder;

  @Column(name = "expiration_date", nullable = false)
  private LocalDate expirationDate;

  @Column(nullable = false)
  private boolean active = true;
}