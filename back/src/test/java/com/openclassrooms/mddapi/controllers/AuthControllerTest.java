package com.openclassrooms.mddapi.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.services.AuthService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AuthController.class)
@Tag("unit")
@DisplayName("AuthController")
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @MockitoBean
  private AuthService authService;

  @Nested
  @Tag("register")
  @DisplayName("POST /api/auth/register")
  class RegisterTests {

    @Test
    @DisplayName("should return 201 and the token when register succeeds")
    void register_shouldReturn201AndToken_whenRequestValid() throws Exception {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com", "jeanbiche", "Password123!");
      AuthResponse response = new AuthResponse("fake.jwt.token");

      when(authService.register(any(RegisterRequest.class))).thenReturn(response);

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.token").value("fake.jwt.token"));

      verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("should return 409 when the user already exists")
    void register_shouldReturn409_whenUserAlreadyExists() throws Exception {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com", "jeanbiche", "Password123!");

      when(authService.register(any(RegisterRequest.class)))
          .thenThrow(new UserAlreadyExistsException("Email is already in use"));

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));
      // THEN
      result.andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 400 when body is missing required fields")
    void register_shouldReturn400_whenFieldsMissing() throws Exception {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "", "", "Password123!");

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

    @Test
    @DisplayName("should return 200 and the token when login succeeds")
    void login_shouldReturn200AndToken_whenCredentialsValid() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest("john.doe@example.com", "Password123!");
      AuthResponse response = new AuthResponse("fake.jwt.token");

      when(authService.login(any(LoginRequest.class))).thenReturn(response);

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));
      // THEN
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.token").value("fake.jwt.token"));
    }

    @Test
    @DisplayName("should return 401 when credentials are invalid")
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest("john.doe@example.com", "wrongPassword123!");

      when(authService.login(any(LoginRequest.class)))
          .thenThrow(new BadCredentialsException("Bad credentials"));

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 400 when request is incomplete")
    void login_shouldReturn400_whenRequestIncomplete() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest("john.doe@example.com", "");

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));
      // THEN
      result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should be accessible without authentication")
    void login_shouldBeAccessible_withoutJwt() throws Exception {
      // GIVEN
      LoginRequest request = new LoginRequest("john.doe@example.com", "Password123!");

      when(authService.login(any(LoginRequest.class)))
          .thenReturn(new AuthResponse("fake.jwt.token"));

      // WHEN
      ResultActions result = mockMvc.perform(post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)));

      // THEN
      result.andExpect(status().isOk());
    }
  }
}