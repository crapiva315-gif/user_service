package dev.alexeev.user_service.mapper;

import dev.alexeev.user_service.dto.card.PaymentCardCreateRequest;
import dev.alexeev.user_service.dto.card.PaymentCardResponseDto;
import dev.alexeev.user_service.dto.card.PaymentCardUpdateRequest;
import dev.alexeev.user_service.entity.PaymentCard;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

  @Mapping(source = "user.id", target = "userId")
  PaymentCardResponseDto toDto(PaymentCard card);

  List<PaymentCardResponseDto> toDtoList(List<PaymentCard> cards);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "active", constant = "true")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  PaymentCard toEntity(PaymentCardCreateRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromDto(PaymentCardUpdateRequest request, @MappingTarget PaymentCard card);
}