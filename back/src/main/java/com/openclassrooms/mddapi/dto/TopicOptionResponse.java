package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Topic option for post creation form")
public record TopicOptionResponse(

        @Schema(description = "Topic identifier", example = "1") Long id,

        @Schema(description = "Topic name", example = "Java") String name

) {
}