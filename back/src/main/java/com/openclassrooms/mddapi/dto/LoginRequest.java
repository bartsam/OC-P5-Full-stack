package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// @formatter:off
@Schema(description = "Login credentials")
public record LoginRequest(

  @Schema(description = "Email address or username used to sign in", example = "john.doe@example.com or jeanbiche")
  @NotBlank(message = "Email or username is required")
  @Size(min = 3, max = 50, message = "Email or username must not exceed 50 characters")
  String identifier,

  @Schema(description = "User password", example = "Password123!", accessMode = Schema.AccessMode.WRITE_ONLY)
  @NotBlank(message = "Password is required")
  String password

) {}
// @formatter:on