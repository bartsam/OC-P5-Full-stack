package com.openclassrooms.mddapi.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.openclassrooms.mddapi.dto.UserResponse;
import com.openclassrooms.mddapi.dto.UserUpdateRequest;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.mappers.UserMapper;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.services.UserService;

import jakarta.persistence.EntityNotFoundException;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(UserController.class)
@Tag("unit")
@DisplayName("UserController")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @Autowired
    private JsonMapper jsonMapper;

    @Nested
    @Tag("getUser")
    @DisplayName("GET /api/profile")
    class GetUserTests {

        @Test
        @DisplayName("should return 200 and the user profile when authenticated")
        void getUser_shouldReturn200AndProfile_whenAuthenticated() throws Exception {
            // GIVEN
            UserEntity user = new UserEntity("john.doe@example.com", "jeanbiche", "encodedPassword123!");
            user.setId(1L);
            UserResponse response = new UserResponse(
                    1L, "john.doe@example.com", "jeanbiche", LocalDateTime.now(),
                    LocalDateTime.now());

            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            when(userService.findById(1L)).thenReturn(user);
            when(userMapper.toDto(user)).thenReturn(response);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/profile")
                    .principal(authentication));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.username").value("jeanbiche"));
        }

        @Test
        @DisplayName("should return 404 when user id from token is not found")
        void getUser_shouldReturn404_whenUserNotFound() throws Exception {
            // GIVEN
            when(userService.findById(99L))
                    .thenThrow(new EntityNotFoundException("User not found with id: 99"));

            Authentication authentication = new UsernamePasswordAuthenticationToken("99", null, List.of());

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/profile")
                    .principal(authentication));

            // THEN
            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @Tag("updateUser")
    @DisplayName("PUT /api/profile")
    class UpdateUserTests {

        @Test
        @DisplayName("should return 200 and updated profile when request is valid")
        void updateUser_shouldReturn200AndUpdatedProfile_whenRequestValid() throws Exception {
            // GIVEN
            UserUpdateRequest request = new UserUpdateRequest(
                    "new.email@example.com", "newUsername", "NewPassword123!");

            UserEntity updatedUser = new UserEntity("new.email@example.com", "newUsername",
                    "encodedPassword");
            updatedUser.setId(1L);

            UserResponse response = new UserResponse(
                    1L, "new.email@example.com", "newUsername", LocalDateTime.now(),
                    LocalDateTime.now());

            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updatedUser);
            when(userMapper.toDto(updatedUser)).thenReturn(response);

            // WHEN
            ResultActions result = mockMvc.perform(put("/api/profile")
                    .principal(authentication)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("new.email@example.com"))
                    .andExpect(jsonPath("$.username").value("newUsername"));
        }

        @Test
        @DisplayName("should return 400 when payload is invalid")
        void updateUser_shouldReturn400_whenPayloadInvalid() throws Exception {
            // GIVEN
            UserUpdateRequest invalidRequest = new UserUpdateRequest("not-an-email", "ab", "weak");

            Authentication authentication = new UsernamePasswordAuthenticationToken("99", null, List.of());

            // WHEN
            ResultActions result = mockMvc.perform(put("/api/profile")
                    .principal(authentication)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsString(invalidRequest)));

            // THEN
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 when user from token does not exist")
        void updateUser_shouldReturn404_whenUserNotFound() throws Exception {
            // GIVEN
            UserUpdateRequest request = new UserUpdateRequest(
                    "new.email@example.com", "newUsername", "NewPassword123!");

            Authentication authentication = new UsernamePasswordAuthenticationToken("99", null, List.of());

            when(userService.updateUser(eq(99L), any(UserUpdateRequest.class)))
                    .thenThrow(new EntityNotFoundException("User not found with id: 99"));

            // WHEN
            ResultActions result = mockMvc.perform(put("/api/profile")
                    .principal(authentication)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 409 when email or username is already used")
        void updateUser_shouldReturn409_whenEmailOrUsernameAlreadyUsed() throws Exception {
            // GIVEN
            UserUpdateRequest request = new UserUpdateRequest(
                    "taken.email@example.com", "newUsername", "NewPassword123!");

            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            when(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
                    .thenThrow(new UserAlreadyExistsException("Email is already in use"));

            // WHEN
            ResultActions result = mockMvc.perform(put("/api/profile")
                    .principal(authentication)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isConflict());
        }
    }
}