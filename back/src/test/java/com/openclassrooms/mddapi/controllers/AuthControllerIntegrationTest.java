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
@DisplayName("AuthController integration tests")
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
  void cleanDatabaseBeforeTest() {
    userRepository.deleteAll();
  }

  @Nested
  @Tag("register")
  @DisplayName("Register user")
  class RegisterTests {
    @Test
    @DisplayName("POST /api/auth/register should create user when email is new")
    void register_shouldCreateUser_whenEmailAvailable() throws Exception {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      // WHEN
      ResultActions response = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isCreated())
          .andExpect(jsonPath("$.token").isNotEmpty());

      UserEntity savedUser = userRepository.findByEmail(request.email()).orElseThrow();
      assertThat(savedUser.getUsername()).isEqualTo(request.username());
      assertThat(passwordEncoder.matches(request.password(), savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("POST /register should return 409 when email exists")
    void register_shouldReturnConflict_whenEmailExists() throws Exception {
      // GIVEN
      UserEntity user = new UserEntity("john.doe@example.com",
          "jeanbiche", passwordEncoder.encode("Password123!"));
      userRepository.save(user);

      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      // WHEN
      ResultActions response = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /register should return 409 when username exists")
    void register_shouldReturnConflict_whenUsernameExists() throws Exception {
      // GIVEN
      UserEntity user = new UserEntity("john.doe@example.com",
          "jeanbiche", passwordEncoder.encode("Password123!"));
      userRepository.save(user);

      RegisterRequest request = new RegisterRequest(
          "jean.biche@example.com",
          "jeanbiche",
          "Password123!");

      // WHEN
      ResultActions response = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 when missing field")
    void register_shouldReturnBadRequest_whenFieldBlank() throws Exception {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "",
          "jeanbiche",
          "Password123!");

      // WHEN
      ResultActions response = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isBadRequest());
    }
  }

  @Nested
  @Tag("login")
  @DisplayName("Login user")
  class LoginTests {
    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() throws Exception {
      // GIVEN
      RegisterRequest registerRequest = new RegisterRequest(
          "john.doe@example.com", "jeanbiche", "Password123!");

      mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(registerRequest)));

      LoginRequest loginRequest = new LoginRequest(
          "john.doe@example.com", "Password123!");

      // WHEN
      ResultActions response = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(loginRequest)));

      // THEN
      response.andExpect(status().isOk())
          .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 when user does not exist")
    void login_shouldReturnUnauthorized_whenUserDoesNotExist() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest(
          "unknown@example.com", "Password123!");

      // WHEN
      ResultActions response = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      assertThat(userRepository.existsByEmail(request.identifier())).isFalse();
      response.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 when password incorrect")
    void login_shouldReturnUnauthorized_whenPasswordIncorrect() throws Exception {
      // GIVEN
      UserEntity user = new UserEntity("john.doe@example.com",
          "jeanbiche", passwordEncoder.encode("Password123!"));
      userRepository.save(user);

      LoginRequest request = new LoginRequest(
          "john.doe@example.com", "WrongPassword123!");

      // WHEN
      ResultActions response = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isUnauthorized());
    }
  }
}