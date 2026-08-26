package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration details for a new user")
public class RegisterRequest {

  @Schema(description = "Unique email address used to sign in", example = "john.doe@example.com")
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 50)
  private String email;

  @Schema(description = "Unique username used to sign in", example = "jeanbiche")
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
  private String username;

  @Schema(description = "Password for the new account", example = "Password123!", accessMode = Schema.AccessMode.WRITE_ONLY)
  @NotBlank(message = "Password is required")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$", message = "Password must contain at least 8 characters, including uppercase, lowercase, digit, and special character")
  private String password;

}
