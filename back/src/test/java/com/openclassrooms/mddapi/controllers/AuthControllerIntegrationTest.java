package com.openclassrooms.mddapi.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("AuthController")
class AuthControllerIntegrationTest {

  @Container
  @ServiceConnection
  static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.4");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void cleanDatabase() {
    userRepository.deleteAll();
  }

  @Nested
  @Tag("register")
  @DisplayName("POST /api/auth/register")
  class RegisterTests {

    @Test
    @DisplayName("should persist the user and return a valid JWT")
    void register_shouldPersistUserAndReturnToken() throws Exception {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isCreated())
          .andExpect(jsonPath("$.token").isNotEmpty());

      UserEntity savedUser = userRepository.findByEmail(request.email()).orElseThrow();
      assertThat(savedUser.getUsername()).isEqualTo(request.username());
      assertThat(passwordEncoder.matches(request.password(), savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("should return 409 when email already exists")
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
      // GIVEN
      UserEntity user = new UserEntity("john.doe@example.com",
          "jeanbiche", passwordEncoder.encode("Password123!"));
      userRepository.save(user);

      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 400 when payload is invalid")
    void register_shouldReturn400_whenPayloadInvalid() throws Exception {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "invalid-email",
          "",
          "");

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));
      // THEN
      result.andExpect(status().isBadRequest());
    }
  }

  @Nested
  @Tag("login")
  @DisplayName("POST /api/auth/login")
  class LoginTests {

    @BeforeEach
    void createUser() {
      userRepository.save(new UserEntity(
          "john.doe@example.com", "jeanbiche", passwordEncoder.encode("Password123!")));
    }

    @Test
    @DisplayName("should return 200 and a valid JWT when credentials are correct")
    void login_shouldReturn200AndToken_whenCredentialsValid() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest(
          "john.doe@example.com", "Password123!");

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("should authenticate using username as identifier")
    void login_shouldReturn200_whenUsingUsernameAsIdentifier() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest(
          "jeanbiche", "Password123!");

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("should return 401 when password is wrong")
    void login_shouldReturn401_whenPasswordWrong() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest(
          "john.doe@example.com", "wrongPassword123!");

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 401 when user does not exist")
    void login_shouldReturn401_whenUserDoesNotExist() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest(
          "unknown@example.com", "wrongPassword123!");

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isUnauthorized());
    }
  }
}