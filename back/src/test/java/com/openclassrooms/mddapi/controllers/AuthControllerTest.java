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
        @DisplayName("should create user when register request is valid")
        void shouldReturnCreatedWhenRegisterRequestValid() throws Exception {
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
        @DisplayName("should return bad request when email is invalid")
        void shouldReturnBadRequestWhenEmailInvalid() throws Exception {
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
                    .andExpect(jsonPath("$.message").value("Email must be valid"));
            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return conflict when email already exists")
        void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
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
                    .andExpect(jsonPath("$.message").value("Email is already in use"));
            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return conflict when username already exists")
        void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest(
                    "john.doe@example.com",
                    "jeanbiche",
                    "Password123!");

            doThrow(new UserAlreadyExistsException("Username is already in use"))
                    .when(authService)
                    .register(any(RegisterRequest.class));

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Username is already in use"));
            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return unauthorized when authentication fails")
        void shouldReturnUnauthorizedWhenAuthenticationFails() throws Exception {
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
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return bad request when email is blank")
        void shouldReturnBadRequestWhenEmailBlank() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest();
            request.setUsername("jeanbiche");
            request.setPassword("Password123!");

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response.andExpect(status().isBadRequest());
            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return bad request when username is blank")
        void shouldReturnBadRequestWhenUsernameBlank() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest();
            request.setEmail("john.doe@example.com");
            request.setPassword("Password123!");

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response.andExpect(status().isBadRequest());
            verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("should return bad request when password is blank")
        void shouldReturnBadRequestWhenPasswordBlank() throws Exception {
            // GIVEN
            RegisterRequest request = new RegisterRequest();
            request.setEmail("john.doe@example.com");
            request.setUsername("jeanbiche");

            // WHEN
            ResultActions response = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            response.andExpect(status().isBadRequest());
            verify(authService, never()).register(any(RegisterRequest.class));
        }

    }
}
