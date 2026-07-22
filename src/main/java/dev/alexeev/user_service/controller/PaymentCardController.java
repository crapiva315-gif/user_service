package dev.alexeev.user_service.controller;

import dev.alexeev.user_service.dto.card.PaymentCardCreateRequest;
import dev.alexeev.user_service.dto.card.PaymentCardResponseDto;
import dev.alexeev.user_service.dto.card.PaymentCardUpdateRequest;
import dev.alexeev.user_service.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class PaymentCardController {

  private final PaymentCardService paymentCardService;

  @GetMapping("/{id}")
  public ResponseEntity<PaymentCardResponseDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(paymentCardService.getById(id));
  }

  @GetMapping
  public ResponseEntity<List<PaymentCardResponseDto>> getByUserId(@RequestParam Long userId) {
    return ResponseEntity.ok(paymentCardService.getByUserId(userId));
  }

  @PostMapping
  public ResponseEntity<PaymentCardResponseDto> create(@Valid @RequestBody PaymentCardCreateRequest request) {
    PaymentCardResponseDto created = paymentCardService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<PaymentCardResponseDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody PaymentCardUpdateRequest request) {
    return ResponseEntity.ok(paymentCardService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    paymentCardService.delete(id);
    return ResponseEntity.noContent().build();
  }
}