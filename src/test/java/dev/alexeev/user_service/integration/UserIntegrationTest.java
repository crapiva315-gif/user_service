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

  @Autowired
  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

  @Test
  void fullFlow_createUser_addCard_getWithCards_update_delete() throws Exception {
    UserCreateRequest createRequest = new UserCreateRequest();
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
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(cardRequest)))
            .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/users/{id}/full", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cards", hasSize(1)))
            .andExpect(jsonPath("$.cards[0].number").value("4111111111111111"));

    UserUpdateRequest updateRequest = new UserUpdateRequest();
    updateRequest.setName("Alexey");
    updateRequest.setSurname("Updated");
    updateRequest.setEmail("integration-test@example.com");
    updateRequest.setBirthDate(LocalDate.of(1995, 3, 12));
    updateRequest.setActive(true);

    mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.surname").value("Updated"));

    mockMvc.perform(delete("/api/v1/users/{id}", userId))
            .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andExpect(status().isNotFound());
  }

  @Test
  void createUser_shouldReturnConflict_whenEmailDuplicate() throws Exception {
    UserCreateRequest request = new UserCreateRequest();
    request.setName("Test");
    request.setSurname("User");
    request.setEmail("duplicate@example.com");
    request.setBirthDate(LocalDate.of(1990, 1, 1));

    mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

    mockMvc.perform(post("/api/v1/users")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
  }

  @Test
  void createUser_shouldReturnBadRequest_whenEmailInvalid() throws Exception {
    UserCreateRequest request = new UserCreateRequest();
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
}