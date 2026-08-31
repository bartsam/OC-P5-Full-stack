package com.openclassrooms.mddapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.dto.UserResponse;
import com.openclassrooms.mddapi.mappers.UserMapper;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;

  public UserController(
      UserService userService, UserMapper userMapper) {
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @Operation(summary = "Get current user profile", description = "Returns the profile (username and email) of the authenticated user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Profile returned successfully"),
      @ApiResponse(responseCode = "401", description = "User not authenticated (invalid or missing JWT)"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/profile")
  public ResponseEntity<UserResponse> getUser(Authentication authentication) {
    UserEntity user = userService.findByEmailOrUsername(authentication.getName());
    return ResponseEntity.ok(userMapper.toDto(user));
  }

  @Operation(summary = "Update current user profile", description = "Updates the email, username, and optionally the password of the authenticated user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid profile data"),
      @ApiResponse(responseCode = "401", description = "User not authenticated (invalid or missing JWT)"),
      @ApiResponse(responseCode = "404", description = "User not found"),
      @ApiResponse(responseCode = "409", description = "Email or username is already in use")
  })
  @SecurityRequirement(name = "bearerAuth")
  @PutMapping("/profile")
  public ResponseEntity<UserResponse> updateProfile(
      Authentication authentication,
      @Valid @RequestBody UpdateUserRequest request) {
    UserEntity user = userService.updateProfile(authentication.getName(), request);
    return ResponseEntity.ok(userMapper.toDto(user));
  }
}
