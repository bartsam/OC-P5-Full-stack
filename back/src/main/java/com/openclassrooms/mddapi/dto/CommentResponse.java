package com.openclassrooms.mddapi.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comment attached to a post")
public record CommentResponse(

        @Schema(description = "Comment ID", example = "1") Long id,

        @Schema(description = "Username of the comment author, null if the user was deleted", example = "johndoe") String author,

        @Schema(description = "Comment content", example = "Great article, thanks for sharing!") String content,

        @Schema(description = "Comment creation date") LocalDateTime createdAt

) {
}