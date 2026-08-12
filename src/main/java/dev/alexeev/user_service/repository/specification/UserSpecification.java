package dev.alexeev.user_service.repository.specification;

import dev.alexeev.user_service.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecification {

  private UserSpecification() {
  }

  public static Specification<User> hasName(String name) {
    return (root, query, cb) ->
            StringUtils.hasText(name)
                    ? cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
                    : null;
  }

  public static Specification<User> hasSurname(String surname) {
    return (root, query, cb) ->
            StringUtils.hasText(surname)
                    ? cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%")
                    : null;
  }

  public static Specification<User> withFilters(String name, String surname) {
    return Specification.allOf(
            hasName(name),
            hasSurname(surname)
    );
  }
}
