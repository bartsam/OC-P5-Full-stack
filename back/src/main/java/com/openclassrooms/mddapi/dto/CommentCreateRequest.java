package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(

        @NotBlank String content

) {
}