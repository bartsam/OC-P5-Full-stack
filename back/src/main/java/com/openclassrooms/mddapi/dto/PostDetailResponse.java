package com.openclassrooms.mddapi.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed post with comments")
public record PostDetailResponse(

        @Schema(description = "Post ID", example = "1") Long id,

        @Schema(description = "Username of the author", example = "johndoe") String author,

        @Schema(description = "Topic name", example = "Java") String topic,

        @Schema(description = "Post title", example = "All about the Java ecosystem") String title,

        @Schema(description = "Full post content", example = "Spring, Jakarta EE, etc.") String content,

        @Schema(description = "Post creation date") LocalDateTime createdAt

) {
}