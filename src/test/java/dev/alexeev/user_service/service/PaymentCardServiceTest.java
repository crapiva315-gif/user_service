package dev.alexeev.user_service.service;

import dev.alexeev.user_service.dto.card.PaymentCardCreateRequest;
import dev.alexeev.user_service.dto.card.PaymentCardResponseDto;
import dev.alexeev.user_service.entity.PaymentCard;
import dev.alexeev.user_service.entity.User;
import dev.alexeev.user_service.exception.InactiveUserException;
import dev.alexeev.user_service.exception.MaxCardsLimitExceededException;
import dev.alexeev.user_service.exception.PaymentCardNotFoundException;
import dev.alexeev.user_service.exception.UserNotFoundException;
import dev.alexeev.user_service.mapper.PaymentCardMapper;
import dev.alexeev.user_service.repository.PaymentCardRepository;
import dev.alexeev.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

  @Mock
  private PaymentCardRepository paymentCardRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PaymentCardMapper paymentCardMapper;

  @InjectMocks
  private PaymentCardService paymentCardService;

  private User user;
  private PaymentCard card;
  private PaymentCardResponseDto cardResponseDto;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setActive(true);

    card = new PaymentCard();
    card.setId(1L);
    card.setUser(user);
    card.setNumber("4111111111111111");
    card.setHolder("ALEXEY PETROV");
    card.setExpirationDate(LocalDate.of(2028, 5, 1));
    card.setActive(true);

    cardResponseDto = new PaymentCardResponseDto();
    cardResponseDto.setId(1L);
    cardResponseDto.setUserId(1L);
  }

  @Test
  void create_shouldSaveCard_whenUserExistsAndActive() {
    PaymentCardCreateRequest request = new PaymentCardCreateRequest();
    request.setUserId(1L);
    request.setNumber("4111111111111111");
    request.setHolder("ALEXEY PETROV");
    request.setExpirationDate(LocalDate.of(2028, 5, 1));

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(paymentCardMapper.toEntity(request)).thenReturn(card);
    when(paymentCardRepository.save(card)).thenReturn(card);
    when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

    PaymentCardResponseDto result = paymentCardService.create(request);

    assertThat(result.getUserId()).isEqualTo(1L);
    verify(paymentCardRepository).save(card);
  }

  @Test
  void create_shouldThrowException_whenUserNotFound() {
    PaymentCardCreateRequest request = new PaymentCardCreateRequest();
    request.setUserId(999L);

    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.create(request))
            .isInstanceOf(UserNotFoundException.class);

    verify(paymentCardRepository, never()).save(any());
  }

  @Test
  void create_shouldThrowException_whenUserIsInactive() {
    user.setActive(false);
    PaymentCardCreateRequest request = new PaymentCardCreateRequest();
    request.setUserId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> paymentCardService.create(request))
            .isInstanceOf(InactiveUserException.class);

    verify(paymentCardRepository, never()).save(any());
  }
  @Test
  void create_shouldThrowException_whenUserAlreadyHasMaxCards() {
    PaymentCardCreateRequest request = new PaymentCardCreateRequest();
    request.setUserId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(paymentCardRepository.countByUserId(1L)).thenReturn(5L);

    assertThatThrownBy(() -> paymentCardService.create(request))
            .isInstanceOf(MaxCardsLimitExceededException.class)
            .hasMessageContaining("1");

    verify(paymentCardRepository, never()).save(any());
  }

  @Test
  void create_shouldSaveCard_whenUserHasFourCards() {
    PaymentCardCreateRequest request = new PaymentCardCreateRequest();
    request.setUserId(1L);
    request.setNumber("4111111111111111");
    request.setHolder("ALEXEY PETROV");
    request.setExpirationDate(LocalDate.of(2028, 5, 1));

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(paymentCardRepository.countByUserId(1L)).thenReturn(4L);
    when(paymentCardMapper.toEntity(request)).thenReturn(card);
    when(paymentCardRepository.save(card)).thenReturn(card);
    when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

    PaymentCardResponseDto result = paymentCardService.create(request);

    assertThat(result.getUserId()).isEqualTo(1L);
    verify(paymentCardRepository).save(card);
  }

  @Test
  void getById_shouldThrowException_whenCardNotFound() {
    when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.getById(999L))
            .isInstanceOf(PaymentCardNotFoundException.class);
  }

  @Test
  void getByUserId_shouldReturnPageOfCards_whenUserExists() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentCard> cardPage = new PageImpl<>(List.of(card), pageable, 1);

    when(userRepository.existsById(1L)).thenReturn(true);
    when(paymentCardRepository.findByUserId(1L, pageable)).thenReturn(cardPage);
    when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

    Page<PaymentCardResponseDto> result = paymentCardService.getByUserId(1L, pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getUserId()).isEqualTo(1L);
  }

  @Test
  void getByUserId_shouldThrowException_whenUserNotFound() {
    Pageable pageable = PageRequest.of(0, 10);
    when(userRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> paymentCardService.getByUserId(999L, pageable))
            .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void delete_shouldDeleteCard_andReturnOwnerUserId_whenCardExists() {
    when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));

    Long returnedUserId = paymentCardService.delete(1L);

    verify(paymentCardRepository).deleteById(1L);
    assertThat(returnedUserId).isEqualTo(1L);
  }

  @Test
  void delete_shouldThrowException_whenCardNotFound() {
    when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.delete(999L))
            .isInstanceOf(PaymentCardNotFoundException.class);
  }
}