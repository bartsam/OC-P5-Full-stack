package com.openclassrooms.mddapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.MessageResponse;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(
      AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new MessageResponse("User registered successfully"));
  }

}
