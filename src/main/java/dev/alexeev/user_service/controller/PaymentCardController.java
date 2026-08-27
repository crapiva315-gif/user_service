package dev.alexeev.user_service.controller;

import dev.alexeev.user_service.dto.card.PaymentCardCreateRequest;
import dev.alexeev.user_service.dto.card.PaymentCardResponseDto;
import dev.alexeev.user_service.dto.card.PaymentCardUpdateRequest;
import dev.alexeev.user_service.security.AccessGuard;
import dev.alexeev.user_service.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class PaymentCardController {

  private final PaymentCardService paymentCardService;
  private final AccessGuard accessGuard;

  @GetMapping("/{id}")
  public ResponseEntity<PaymentCardResponseDto> getById(
          @PathVariable Long id,
          @RequestHeader(value = "X-User-Id", required = false) Long callerUserId,
          @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
    PaymentCardResponseDto card = paymentCardService.getById(id);
    accessGuard.requireSelfOrAdmin(card.getUserId(), callerUserId, callerRole);
    return ResponseEntity.ok(card);
  }

  @GetMapping
  public ResponseEntity<Page<PaymentCardResponseDto>> getByUserId(
          @RequestParam(required = false) Long userId,
          Pageable pageable,
          @RequestHeader(value = "X-User-Id", required = false) Long callerUserId,
          @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
    boolean isAdmin = "ADMIN".equalsIgnoreCase(callerRole);
    Long effectiveUserId = isAdmin && userId != null ? userId : callerUserId;
    accessGuard.requireSelfOrAdmin(effectiveUserId, callerUserId, callerRole);
    return ResponseEntity.ok(paymentCardService.getByUserId(effectiveUserId, pageable));
  }

  @PostMapping
  public ResponseEntity<PaymentCardResponseDto> create(
          @Valid @RequestBody PaymentCardCreateRequest request,
          @RequestHeader(value = "X-User-Id", required = false) Long callerUserId,
          @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
    accessGuard.requireSelfOrAdmin(request.getUserId(), callerUserId, callerRole);
    PaymentCardResponseDto created = paymentCardService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<PaymentCardResponseDto> update(
          @PathVariable Long id,
          @Valid @RequestBody PaymentCardUpdateRequest request,
          @RequestHeader(value = "X-User-Id", required = false) Long callerUserId,
          @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
    PaymentCardResponseDto existing = paymentCardService.getById(id);
    accessGuard.requireSelfOrAdmin(existing.getUserId(), callerUserId, callerRole);
    return ResponseEntity.ok(paymentCardService.update(id, request));
  }

  @PatchMapping("/{id}/activate")
  public ResponseEntity<Void> activate(
          @PathVariable Long id,
          @RequestHeader(value = "X-User-Id", required = false) Long callerUserId,
          @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
    PaymentCardResponseDto existing = paymentCardService.getById(id);
    accessGuard.requireSelfOrAdmin(existing.getUserId(), callerUserId, callerRole);
    paymentCardService.activate(id);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<Void> deactivate(
          @PathVariable Long id,
          @RequestHeader(value = "X-User-Id", required = false) Long callerUserId,
          @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
    PaymentCardResponseDto existing = paymentCardService.getById(id);
    accessGuard.requireSelfOrAdmin(existing.getUserId(), callerUserId, callerRole);
    paymentCardService.deactivate(id);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
          @PathVariable Long id,
          @RequestHeader(value = "X-User-Id", required = false) Long callerUserId,
          @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
    PaymentCardResponseDto existing = paymentCardService.getById(id);
    accessGuard.requireSelfOrAdmin(existing.getUserId(), callerUserId, callerRole);
    paymentCardService.delete(id);
    return ResponseEntity.noContent().build();
  }
}