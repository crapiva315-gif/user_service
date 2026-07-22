package dev.alexeev.user_service.mapper;

import dev.alexeev.user_service.dto.user.UserCreateRequest;
import dev.alexeev.user_service.dto.user.UserResponseDto;
import dev.alexeev.user_service.dto.user.UserUpdateRequest;
import dev.alexeev.user_service.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserResponseDto toDto(User user);

  List<UserResponseDto> toDtoList(List<User> users);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "active", constant = "true")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "cards", ignore = true)
  User toEntity(UserCreateRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "cards", ignore = true)
  void updateEntityFromDto(UserUpdateRequest request, @MappingTarget User user);
}