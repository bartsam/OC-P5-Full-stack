package com.openclassrooms.mddapi.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Post item in feed list")
public record PostItemResponse(

        @Schema(description = "Post ID", example = "1") Long id,

        @Schema(description = "Post title", example = "All about the Java ecosystem") String title,

        @Schema(description = "Truncated content preview", example = "Spring, Jakarta EE, etc.") String content,

        @Schema(description = "Post creation date") LocalDateTime createdAt

) {
}