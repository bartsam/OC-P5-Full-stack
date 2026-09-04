package com.openclassrooms.mddapi.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User profile information")
public record UserResponse(

    @Schema(description = "Unique user identifier", example = "1") Long id,

    @Schema(description = "Email address of the user", example = "john.doe@example.com") String email,

    @Schema(description = "Unique username of the user", example = "jeanbiche") String username,

    @Schema(description = "Profile creation date") LocalDateTime createdAt,

    @Schema(description = "Profile last update date") LocalDateTime updatedAt

) {
}
