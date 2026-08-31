package com.openclassrooms.mddapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(
      AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Register new user", description = "Creates new user and authenticates it immediately. Returns a JWT token for authenticated calls")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User created and JWT token returned"),
      @ApiResponse(responseCode = "400", description = "Invalid data (incorrect format, missing fields, etc.)"),
      @ApiResponse(responseCode = "409", description = "User already exists (duplicate email or username)")
  })
  @SecurityRequirements
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request) {

    AuthResponse authResponse = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
  }

  @Operation(summary = "Authenticate user", description = "Authenticates user with email/username and password. Returns a JWT token for authenticated calls.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authentication successful, JWT token returned"),
      @ApiResponse(responseCode = "400", description = "Invalid credentials (incorrect format, missing fields)"),
      @ApiResponse(responseCode = "401", description = "Invalid email/username or password")
  })
  @SecurityRequirements
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

}
