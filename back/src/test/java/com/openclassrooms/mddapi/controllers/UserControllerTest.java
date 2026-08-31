package com.openclassrooms.mddapi.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.openclassrooms.mddapi.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.dto.UserResponse;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.mappers.UserMapper;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.services.UserService;

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
        @DisplayName("getUser")
        class GetUserTests {

                @Test
                @DisplayName("should return username and email when token is valid")
                void getUser_shouldReturnUser_whenTokenValid() throws Exception {
                        // GIVEN
                        UserEntity user = new UserEntity(
                                        "john.doe@example.com",
                                        "jeanbiche",
                                        "Password123!");

                        UserResponse userResponse = new UserResponse(
                                        1L,
                                        "john.doe@example.com",
                                        "jeanbiche",
                                        null,
                                        null);

                        Authentication authentication = new UsernamePasswordAuthenticationToken("john.doe@example.com",
                                        "Password123!");

                        when(userService.findByEmailOrUsername("john.doe@example.com"))
                                        .thenReturn(user);

                        when(userMapper.toDto(user))
                                        .thenReturn(userResponse);

                        // WHEN
                        ResultActions response = mockMvc.perform(get("/api/profile")
                                        .principal(authentication));

                        // THEN
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.username").value("jeanbiche"))
                                        .andExpect(jsonPath("$.email").value("john.doe@example.com"));

                        verify(userService).findByEmailOrUsername("john.doe@example.com");
                        verify(userMapper).toDto(user);
                }

                @Test
                @DisplayName("should return 404 when user is not found")
                void getUser_shouldReturn404_whenUserNotFound() throws Exception {
                        // GIVEN
                        Authentication authentication = new UsernamePasswordAuthenticationToken("unknown@example.com",
                                        "Password123!");

                        when(userService.findByEmailOrUsername("unknown@example.com"))
                                        .thenThrow(new UserNotFoundException("User not found"));

                        // WHEN
                        ResultActions response = mockMvc.perform(get("/api/profile")
                                        .principal(authentication));

                        // THEN
                        response.andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("updateUser")
        class updateUserTests {

                @Test
                @DisplayName("should return 200 when profile is updated successfully")
                void updateUser_shouldReturn200_whenSuccess() throws Exception {
                        // GIVEN
                        String email = "john.doe@example.com";
                        UpdateUserRequest request = new UpdateUserRequest(
                                        "new.john@example.com",
                                        "newjeanbiche",
                                        "NewPassword123!");

                        UserEntity updatedUser = new UserEntity(
                                        "new.john@example.com",
                                        "newjeanbiche",
                                        "EncodedNewPassword123!");

                        UserResponse userResponse = new UserResponse(
                                        1L,
                                        "new.john@example.com",
                                        "newjeanbiche",
                                        null,
                                        null);

                        Authentication authentication = new UsernamePasswordAuthenticationToken(email, "Password123!");

                        when(userService.updateUser(eq(email), any(UpdateUserRequest.class)))
                                        .thenReturn(updatedUser);

                        when(userMapper.toDto(updatedUser))
                                        .thenReturn(userResponse);

                        // WHEN
                        ResultActions response = mockMvc.perform(put("/api/profile")
                                        .principal(authentication)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(request)));

                        // THEN
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.email").value("new.john@example.com"))
                                        .andExpect(jsonPath("$.username").value("newjeanbiche"));

                        verify(userService).updateUser(eq(email), any(UpdateUserRequest.class));
                        verify(userMapper).toDto(updatedUser);
                }

                @Test
                @DisplayName("should return 404 when user is not found")
                void updateUser_shouldReturn404_whenUserNotFound() throws Exception {
                        // GIVEN
                        String email = "john.doe@example.com";
                        UpdateUserRequest request = new UpdateUserRequest(
                                        "new.john@example.com",
                                        "newjeanbiche",
                                        "NewPassword123!");

                        Authentication authentication = new UsernamePasswordAuthenticationToken(email, "Password123!");

                        when(userService.updateUser(eq(email), any(UpdateUserRequest.class)))
                                        .thenThrow(new UserNotFoundException("User not found"));

                        // WHEN
                        ResultActions response = mockMvc.perform(put("/api/profile")
                                        .principal(authentication)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(request)));

                        // THEN
                        response.andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("should return 409 when email or username is already in use")
                void updateUser_shouldReturn409_whenConflict() throws Exception {
                        // GIVEN
                        String email = "john.doe@example.com";
                        UpdateUserRequest request = new UpdateUserRequest(
                                        "duplicate@example.com",
                                        "duplicateUser",
                                        "Password123!");

                        Authentication authentication = new UsernamePasswordAuthenticationToken(email, "Password123!");

                        when(userService.updateUser(eq(email), any(UpdateUserRequest.class)))
                                        .thenThrow(new UserAlreadyExistsException(
                                                        "Email or username is already in use"));

                        // WHEN
                        ResultActions response = mockMvc.perform(put("/api/profile")
                                        .principal(authentication)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(request)));

                        // THEN
                        response.andExpect(status().isConflict());
                }

                @Test
                @DisplayName("should return 400 when request body is invalid")
                void updateUser_shouldReturn400_whenInvalidBody() throws Exception {
                        // GIVEN
                        String email = "john.doe@example.com";
                        UpdateUserRequest invalidRequest = new UpdateUserRequest(
                                        "invalid-email",
                                        "ab",
                                        "Password123!");

                        Authentication authentication = new UsernamePasswordAuthenticationToken(email, "Password123!");

                        // WHEN
                        ResultActions response = mockMvc.perform(put("/api/profile")
                                        .principal(authentication)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(invalidRequest)));

                        // THEN
                        response.andExpect(status().isBadRequest());
                }
        }
}