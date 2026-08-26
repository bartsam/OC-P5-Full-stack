package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing a descriptive message")
public record MessageResponse(
    @Schema(description = "Response message", example = "An internal error has occurred") String message) {
}