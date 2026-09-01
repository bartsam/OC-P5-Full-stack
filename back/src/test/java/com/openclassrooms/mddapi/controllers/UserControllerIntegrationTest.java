package com.openclassrooms.mddapi.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("UserController")
public class UserControllerIntegrationTest {

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

  @Autowired
  private JwtService jwtService;

  private UserEntity existingUser;
  private String validToken;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    existingUser = userRepository.save(new UserEntity(
        "john.doe@example.com", "jeanbiche", passwordEncoder.encode("Password123!")));

    validToken = jwtService.generateToken(existingUser.getId());
  }

  @Nested
  @Tag("getUser")
  @DisplayName("GET /api/profile")
  class GetProfileTests {

    @Test
    @DisplayName("should return 200 and the profile when JWT is valid")
    void getUser_shouldReturn200AndProfile_whenTokenValid() throws Exception {
      // WHEN
      ResultActions result = mockMvc.perform(get("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

      // THEN
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(existingUser.getId()))
          .andExpect(jsonPath("$.email").value("john.doe@example.com"))
          .andExpect(jsonPath("$.username").value("jeanbiche"));
    }

    @Test
    @DisplayName("should return 401 when no Authorization header is provided")
    void getUser_shouldReturn401_whenNoTokenProvided() throws Exception {
      // WHEN
      ResultActions result = mockMvc.perform(get("/api/profile"));

      // THEN
      result
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("should return 401 when JWT is malformed")
    void getUser_shouldReturn401_whenTokenMalformed() throws Exception {
      // WHEN
      ResultActions result = mockMvc.perform(get("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt"));

      // THEN
      result
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("should return 404 when user from token no longer exists")
    void getUser_shouldReturn404_whenUserDeleted() throws Exception {
      // GIVEN
      String tokenForDeletedUser = jwtService.generateToken(existingUser.getId());
      userRepository.deleteAll();

      // WHEN
      ResultActions result = mockMvc.perform(get("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenForDeletedUser));

      // THEN
      result.andExpect(status().isNotFound());
    }
  }

  @Nested
  @Tag("updateUser")
  @DisplayName("PUT /api/profile")
  class UpdateUserTests {

    @Test
    @DisplayName("should update profile and persist changes in database")
    void updateUser_shouldUpdateAndPersist_whenRequestValid() throws Exception {
      // GIVEN
      UpdateUserRequest request = new UpdateUserRequest(
          "updated.email@example.com", "updatedUsername", "UpdatedPassword123!");

      // WHEN
      ResultActions result = mockMvc.perform(put("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.email").value(request.email()))
          .andExpect(jsonPath("$.username").value(request.username()));

      UserEntity updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
      assertThat(updatedUser.getUsername()).isEqualTo(request.username());
      assertThat(updatedUser.getUsername()).isEqualTo(request.username());
      assertThat(passwordEncoder.matches(request.password(), updatedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("should return 401 when no Authorization header is provided")
    void updateUser_shouldReturn401_whenNoTokenProvided() throws Exception {
      // GIVEN
      UpdateUserRequest request = new UpdateUserRequest(
          "updated.john@example.com", "updatedUsername", "UpdatedPassword123!");
      // WHEN
      ResultActions result = mockMvc.perform(put("/api/profile")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 400 when payload is invalid")
    void updateUser_shouldReturn400_whenPayloadInvalid() throws Exception {
      // GIVEN
      UpdateUserRequest request = new UpdateUserRequest(
          "invalide-email", "un", "weakPassword");

      // WHEN
      ResultActions result = mockMvc.perform(put("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 409 when email is already used by another user")
    void updateUser_shouldReturn409_whenEmailAlreadyUsedByAnotherUser() throws Exception {
      // GIVEN
      userRepository.save(new UserEntity(
          "taken.email@example.com", "otherUser", passwordEncoder.encode("Password123!")));

      UpdateUserRequest request = new UpdateUserRequest(
          "taken.email@example.com", "jeanbiche", "Password123!");

      // WHEN
      ResultActions result = mockMvc.perform(put("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should allow update when email and username are unchanged")
    void updateUser_shouldReturn200_whenEmailAndUsernameUnchanged() throws Exception {
      // GIVEN
      UpdateUserRequest request = new UpdateUserRequest(
          "john.doe@example.com", "jeanbiche", "newPassword123!");

      // WHEN
      ResultActions result = mockMvc.perform(put("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isOk());
    }
  }
}