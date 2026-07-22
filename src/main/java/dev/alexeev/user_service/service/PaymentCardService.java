package dev.alexeev.user_service.service;

import dev.alexeev.user_service.dto.card.*;
import dev.alexeev.user_service.entity.PaymentCard;
import dev.alexeev.user_service.entity.User;
import dev.alexeev.user_service.mapper.PaymentCardMapper;
import dev.alexeev.user_service.repository.PaymentCardRepository;
import dev.alexeev.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardService {

  private final PaymentCardRepository paymentCardRepository;
  private final UserRepository userRepository;
  private final PaymentCardMapper paymentCardMapper;

  @Transactional(readOnly = true)
  public PaymentCardResponseDto getById(Long id) {
    PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment card with id=" + id + " not found"));
    return paymentCardMapper.toDto(card);
  }

  @Transactional(readOnly = true)
  public List<PaymentCardResponseDto> getByUserId(Long userId) {
    return paymentCardMapper.toDtoList(paymentCardRepository.findByUserId(userId));
  }

  @Transactional
  public PaymentCardResponseDto create(PaymentCardCreateRequest request) {
    User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new EntityNotFoundException(
                    "User with id=" + request.getUserId() + " not found"));

    PaymentCard card = paymentCardMapper.toEntity(request);
    card.setUser(user);
    return paymentCardMapper.toDto(paymentCardRepository.save(card));
  }

  @Transactional
  public PaymentCardResponseDto update(Long id, PaymentCardUpdateRequest request) {
    PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment card with id=" + id + " not found"));
    paymentCardMapper.updateEntityFromDto(request, card);
    return paymentCardMapper.toDto(card);
  }

  @Transactional
  public void delete(Long id) {
    if (!paymentCardRepository.existsById(id)) {
      throw new EntityNotFoundException("Payment card with id=" + id + " not found");
    }
    paymentCardRepository.deleteById(id);
  }
}