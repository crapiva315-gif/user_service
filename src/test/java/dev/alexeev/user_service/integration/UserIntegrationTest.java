package dev.alexeev.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexeev.user_service.dto.card.PaymentCardCreateRequest;
import dev.alexeev.user_service.dto.user.UserCreateRequest;
import dev.alexeev.user_service.dto.user.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import static org.hamcrest.Matchers.hasSize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
          .withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
  }

  private static final AtomicLong ID_SEQUENCE = new AtomicLong(1);
  private static final String ADMIN_ROLE_HEADER = "X-User-Role";
  private static final String ADMIN_ROLE = "ADMIN";

  @Autowired
  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

  @Test
  void fullFlow_createUser_addCard_getWithCards_update_delete() throws Exception {
    UserCreateRequest createRequest = new UserCreateRequest();
    createRequest.setId(ID_SEQUENCE.getAndIncrement());
    createRequest.setName("Alexey");
    createRequest.setSurname("Petrov");
    createRequest.setEmail("integration-test@example.com");
    createRequest.setBirthDate(LocalDate.of(1995, 3, 12));

    String createResponse = mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("integration-test@example.com"))
            .andReturn().getResponse().getContentAsString();

    Long userId = objectMapper.readTree(createResponse).get("id").asLong();

    PaymentCardCreateRequest cardRequest = new PaymentCardCreateRequest();
    cardRequest.setUserId(userId);
    cardRequest.setNumber("4111111111111111");
    cardRequest.setHolder("ALEXEY PETROV");
    cardRequest.setExpirationDate(LocalDate.of(2028, 5, 1));

    mockMvc.perform(post("/api/v1/cards")
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(cardRequest)))
            .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/users/{id}/full", userId)
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cards", hasSize(1)))
            .andExpect(jsonPath("$.cards[0].number").value("4111111111111111"));

    UserUpdateRequest updateRequest = new UserUpdateRequest();
    updateRequest.setName("Alexey");
    updateRequest.setSurname("Updated");
    updateRequest.setEmail("integration-test@example.com");
    updateRequest.setBirthDate(LocalDate.of(1995, 3, 12));

    mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.surname").value("Updated"));

    mockMvc.perform(delete("/api/v1/users/{id}", userId)
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE))
            .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/users/{id}", userId)
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE))
            .andExpect(status().isNotFound());
  }

  @Test
  void createUser_shouldReturnConflict_whenEmailDuplicate() throws Exception {
    UserCreateRequest request = new UserCreateRequest();
    request.setId(ID_SEQUENCE.getAndIncrement());
    request.setName("Test");
    request.setSurname("User");
    request.setEmail("duplicate@example.com");
    request.setBirthDate(LocalDate.of(1990, 1, 1));

    mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

    request.setId(ID_SEQUENCE.getAndIncrement());
    mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
  }

  @Test
  void createUser_shouldReturnBadRequest_whenEmailInvalid() throws Exception {
    UserCreateRequest request = new UserCreateRequest();
    request.setId(ID_SEQUENCE.getAndIncrement());
    request.setName("Test");
    request.setSurname("User");
    request.setEmail("not-an-email");
    request.setBirthDate(LocalDate.of(1990, 1, 1));

    mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.email").exists());
  }

  @Test
  void getAll_shouldReturnPagedAndFilteredUsers_bySurname() throws Exception {
    UserCreateRequest first = new UserCreateRequest();
    first.setId(ID_SEQUENCE.getAndIncrement());
    first.setName("Ivan");
    first.setSurname("Sidorov");
    first.setEmail("ivan.sidorov@example.com");
    first.setBirthDate(LocalDate.of(1990, 1, 1));

    UserCreateRequest second = new UserCreateRequest();
    second.setId(ID_SEQUENCE.getAndIncrement());
    second.setName("Petr");
    second.setSurname("Ivanov");
    second.setEmail("petr.ivanov@example.com");
    second.setBirthDate(LocalDate.of(1990, 1, 1));

    mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(first)))
            .andExpect(status().isCreated());

    mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(second)))
            .andExpect(status().isCreated());

    // getAll is admin-only now.
    mockMvc.perform(get("/api/v1/users")
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE)
                    .param("surname", "sidorov")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].surname").value("Sidorov"));
  }

  @Test
  void createCard_shouldReturnConflict_whenUserAlreadyHasFiveCards() throws Exception {
    UserCreateRequest userRequest = new UserCreateRequest();
    userRequest.setId(ID_SEQUENCE.getAndIncrement());
    userRequest.setName("Card");
    userRequest.setSurname("Limit");
    userRequest.setEmail("card.limit@example.com");
    userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

    String userResponse = mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

    Long userId = objectMapper.readTree(userResponse).get("id").asLong();

    for (int i = 0; i < 5; i++) {
      PaymentCardCreateRequest cardRequest = new PaymentCardCreateRequest();
      cardRequest.setUserId(userId);
      cardRequest.setNumber("411111111111111" + i);
      cardRequest.setHolder("CARD LIMIT");
      cardRequest.setExpirationDate(LocalDate.of(2028, 5, 1));

      mockMvc.perform(post("/api/v1/cards")
                      .header(ADMIN_ROLE_HEADER, ADMIN_ROLE)
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(cardRequest)))
              .andExpect(status().isCreated());
    }

    PaymentCardCreateRequest sixthCard = new PaymentCardCreateRequest();
    sixthCard.setUserId(userId);
    sixthCard.setNumber("4111111111111199");
    sixthCard.setHolder("CARD LIMIT");
    sixthCard.setExpirationDate(LocalDate.of(2028, 5, 1));

    mockMvc.perform(post("/api/v1/cards")
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(sixthCard)))
            .andExpect(status().isConflict());
  }

  @Test
  void deactivateUser_shouldPreventAddingNewCard() throws Exception {
    UserCreateRequest userRequest = new UserCreateRequest();
    userRequest.setId(ID_SEQUENCE.getAndIncrement());
    userRequest.setName("Deactivated");
    userRequest.setSurname("User");
    userRequest.setEmail("deactivated.user@example.com");
    userRequest.setBirthDate(LocalDate.of(1990, 1, 1));

    String userResponse = mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

    Long userId = objectMapper.readTree(userResponse).get("id").asLong();

    // activate/deactivate are admin-only now.
    mockMvc.perform(patch("/api/v1/users/{id}/deactivate", userId)
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE))
            .andExpect(status().isOk());

    PaymentCardCreateRequest cardRequest = new PaymentCardCreateRequest();
    cardRequest.setUserId(userId);
    cardRequest.setNumber("4111111111111188");
    cardRequest.setHolder("DEACTIVATED USER");
    cardRequest.setExpirationDate(LocalDate.of(2028, 5, 1));

    mockMvc.perform(post("/api/v1/cards")
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(cardRequest)))
            .andExpect(status().isConflict());

    mockMvc.perform(patch("/api/v1/users/{id}/activate", userId)
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE))
            .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/cards")
                    .header(ADMIN_ROLE_HEADER, ADMIN_ROLE)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(cardRequest)))
            .andExpect(status().isCreated());
  }
}