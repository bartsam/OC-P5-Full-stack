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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.openclassrooms.mddapi.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.services.JwtService;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("UserController integration tests")
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

  @BeforeEach
  void cleanDatabaseBeforeTest() {
    userRepository.deleteAll();
  }

  @Nested
  @Tag("getUser")
  @DisplayName("Get User")
  class getUserTests {
    @Test
    @DisplayName("GET /api/profile should return user when exists")
    void getUser_shouldReturnUser_whenExists() throws Exception {
      // GIVEN
      UserEntity user = new UserEntity("john.doe@example.com",
          "jeanbiche", passwordEncoder.encode("Password123!"));
      userRepository.save(user);

      String token = jwtService.generateToken(
          new UsernamePasswordAuthenticationToken("john.doe@example.com", "Password123!"));

      // WHEN
      ResultActions response = mockMvc.perform(get("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username").value("jeanbiche"));

      // THEN
      response.andExpect(status().isOk()).andExpect(jsonPath("$.username").value("jeanbiche"));
    }

    @Test
    @DisplayName("GET /api/profile should return 401 when not authenticated")
    void getUser_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/profile"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/profile should return 404 when user does not exist")
    void getUser_shouldReturnNotFound_whenDoesNotExist() throws Exception {
      String token = jwtService.generateToken(
          new UsernamePasswordAuthenticationToken("unknown@example.com", "Password123!"));

      mockMvc.perform(get("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @Tag("updateUser")
  @DisplayName("Update User")
  class updateUserTests {

    @Test
    @DisplayName("PUT /api/profile should update the authenticated user")
    void updateUser_shouldReturnUpdatedUser_whenRequestIsValid() throws Exception {

      // GIVEN
      UserEntity user = new UserEntity(
          "john.doe@example.com", "jeanbiche", passwordEncoder.encode("Password123!"));
      userRepository.save(user);

      UpdateUserRequest request = new UpdateUserRequest(
          "new.john@example.com", "newjeanbiche", "NewPassword123!");

      String token = jwtService.generateToken(
          new UsernamePasswordAuthenticationToken("john.doe@example.com", "Password123!"));

      // WHEN
      ResultActions response = mockMvc.perform(put("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isOk())
          .andExpect(jsonPath("$.email").value(request.email()))
          .andExpect(jsonPath("$.username").value(request.username()));

      UserEntity updatedUser = userRepository.findByEmail(request.email()).orElseThrow();
      assertThat(updatedUser.getUsername()).isEqualTo(request.username());
      assertThat(passwordEncoder.matches(request.password(), updatedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("PUT /api/profile should return 401 when no token is provided")
    void updateUser_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {

      // GIVEN
      UpdateUserRequest request = new UpdateUserRequest(
          "new.john@example.com", "newjeanbiche", "NewPassword123!");

      // WHEN
      ResultActions response = mockMvc.perform(put("/api/profile")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/profile should return 404 when the token subject has no user")
    void updateUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      // GIVEN
      UpdateUserRequest request = new UpdateUserRequest(
          "new.john@example.com", "newjeanbiche", "NewPassword123!");

      String token = jwtService.generateToken(
          new UsernamePasswordAuthenticationToken("john.doe@example.com", "Password123!"));

      // WHEN
      ResultActions response = mockMvc.perform(put("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/profile should return 400 when profile data is invalid")
    void updateUser_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
      // GIVEN
      UserEntity user = new UserEntity(
          "john.doe@example.com", "jeanbiche", passwordEncoder.encode("Password123!"));
      userRepository.save(user);

      UpdateUserRequest request = new UpdateUserRequest(
          "invalid-email", "newjeanbiche", "NewPassword123!");

      String token = jwtService.generateToken(
          new UsernamePasswordAuthenticationToken("john.doe@example.com", "Password123!"));

      // WHEN
      ResultActions response = mockMvc.perform(put("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/profile should return 409 when email belongs to another user")
    void updateUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
      // GIVEN
      UserEntity userA = new UserEntity(
          "john.doe@example.com", "jeanbiche", passwordEncoder.encode("Password123!"));
      UserEntity userB = new UserEntity(
          "jane.doe@example.com", "janebiche", passwordEncoder.encode("Password123!"));

      userRepository.save(userA);
      userRepository.save(userB);

      UpdateUserRequest request = new UpdateUserRequest(
          "jane.doe@example.com", "newjeanbiche", "NewPassword123!");

      String token = jwtService.generateToken(
          new UsernamePasswordAuthenticationToken("john.doe@example.com", "Password123!"));

      // WHEN
      ResultActions response = mockMvc.perform(put("/api/profile")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      response.andExpect(status().isConflict());
    }

  }

}
