package com.openclassrooms.mddapi.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
    @DisplayName("register")
    class RegisterTests {
        @Test
        @DisplayName("should return token when request is valid")
        void register_shouldReturnToken_whenRequestValid() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest(
                    "john.doe@example.com",
                    "jeanbiche",
                    "Password123!");

            when(authService.register(any(RegisterRequest.class)))
                    .thenReturn(new AuthResponse("fake.jwt.token"));

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("fake.jwt.token"));

            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return 400 when email is invalid")
        void register_shouldReturn400_whenEmailInvalid() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest(
                    "john.doe[at]example.com",
                    "jeanbiche",
                    "Password123!");

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return 409 when identifier already exists")
        void register_shouldReturn409_whenIdentifierAlreadyExists() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest(
                    "john.doe@example.com",
                    "jeanbiche",
                    "Password123!");

            doThrow(new UserAlreadyExistsException("Email is already in use"))
                    .when(authService)
                    .register(any(RegisterRequest.class));

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists());
            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return 401 when authentication fails")
        void register_shouldReturn401_whenAuthenticationFails() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest(
                    "john.doe@example.com",
                    "jeanbiche",
                    "Password123!");

            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authService)
                    .register(any(RegisterRequest.class));

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists());
            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return 400 when request incomplete")
        void register_shouldReturn400_whenRequestIncomplete() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest("", "jeanbiche", "Password123!");

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response.andExpect(status().isBadRequest());
            verify(authService, never()).register(any(RegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {
        @Test
        @DisplayName("should return token when request is valid")
        void login_shouldReturnToken_whenCredentialsValid() throws Exception {
            // GIVEN
            LoginRequest request = new LoginRequest(
                    "john.doe@example.com",
                    "Password123!");

            when(authService.login(any(LoginRequest.class)))
                    .thenReturn(new AuthResponse("fake.jwt.token"));

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("fake.jwt.token"));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("should return 401 when credentials are invalid")
        void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
            // GIVEN
            LoginRequest request = new LoginRequest(
                    "john.doe@example.com",
                    "Password123!");

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists());
            ;
        }

        @Test
        @DisplayName("should return 400 when request is incomplete")
        void login_shouldReturn400_whenRequestIncomplete() throws Exception {
            // GIVEN
            LoginRequest request = new LoginRequest("john.doe@example.com", "");

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));
            // THEN
            response
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
            ;
        }
    }
}
