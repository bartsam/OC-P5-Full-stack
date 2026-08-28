package com.openclassrooms.mddapi.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.openclassrooms.mddapi.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.mappers.UserMapper;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.services.UserService;

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

      Authentication authentication = new UsernamePasswordAuthenticationToken("john.doe@example.com", "Password123!");

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
      Authentication authentication = new UsernamePasswordAuthenticationToken("unknown@example.com", "Password123!");

      when(userService.findByEmailOrUsername("unknown@example.com"))
          .thenThrow(new UserNotFoundException("User not found"));

      // WHEN
      ResultActions response = mockMvc.perform(get("/api/profile")
          .principal(authentication));

      // THEN
      response.andExpect(status().isNotFound());
    }
  }
}