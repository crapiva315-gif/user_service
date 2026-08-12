package dev.alexeev.user_service.service;

import dev.alexeev.user_service.dto.card.*;
import dev.alexeev.user_service.entity.PaymentCard;
import dev.alexeev.user_service.entity.User;
import dev.alexeev.user_service.exception.InactiveUserException;
import dev.alexeev.user_service.exception.MaxCardsLimitExceededException;
import dev.alexeev.user_service.exception.PaymentCardNotFoundException;
import dev.alexeev.user_service.exception.UserNotFoundException;
import dev.alexeev.user_service.mapper.PaymentCardMapper;
import dev.alexeev.user_service.repository.PaymentCardRepository;
import dev.alexeev.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCardService {
  private static final int MAX_CARDS_PER_USER = 5;
  private static final String USER_WITH_CARDS_CACHE = "userWithCards";

  private final PaymentCardRepository paymentCardRepository;
  private final UserRepository userRepository;
  private final PaymentCardMapper paymentCardMapper;

  @Transactional(readOnly = true)
  public PaymentCardResponseDto getById(Long id) {
    PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new PaymentCardNotFoundException(id));
    return paymentCardMapper.toDto(card);
  }

  @Transactional(readOnly = true)
  public Page<PaymentCardResponseDto> getByUserId(Long userId, Pageable pageable) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    return paymentCardRepository.findByUserId(userId, pageable)
            .map(paymentCardMapper::toDto);
  }

  @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#request.userId")
  @Transactional
  public PaymentCardResponseDto create(PaymentCardCreateRequest request) {
    User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

    if (!user.isActive()) {
      throw new InactiveUserException(user.getId());
    }

    if (paymentCardRepository.countByUserId(request.getUserId()) >= MAX_CARDS_PER_USER) {
      throw new MaxCardsLimitExceededException(request.getUserId());
    }

    PaymentCard card = paymentCardMapper.toEntity(request);
    card.setUser(user);
    return paymentCardMapper.toDto(paymentCardRepository.save(card));
  }

  @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#result.userId")
  @Transactional
  public PaymentCardResponseDto update(Long id, PaymentCardUpdateRequest request) {
    PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new PaymentCardNotFoundException(id));
    paymentCardMapper.updateEntityFromDto(request, card);
    return paymentCardMapper.toDto(card);
  }

  @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#result")
  @Transactional
  public Long delete(Long id) {
    PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new PaymentCardNotFoundException(id));
    Long userId = card.getUser().getId();
    paymentCardRepository.deleteById(id);
    return userId;
  }
}