package com.openclassrooms.mddapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed topic with the current user's subscription status")
public record TopicItemResponse(

    @Schema(description = "Topic identifier", example = "1") Long id,

    @Schema(description = "Unique name of the topic", example = "Java") String name,

    @Schema(description = "Description of the topic", example = "All about the Java ecosystem: Spring, Jakarta EE, etc.") String description,

    @Schema(description = "Whether the current user is subscribed to this topic", example = "true") boolean isSubscribed

) {
}