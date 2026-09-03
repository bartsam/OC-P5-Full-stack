package com.openclassrooms.mddapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.TopicItemResponse;
import com.openclassrooms.mddapi.dto.TopicOptionResponse;
import com.openclassrooms.mddapi.services.TopicService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    /**
     * Constructs the TopicController with the required TopicService dependency.
     *
     * @param topicService the service responsible for managing topics and
     *                     subscription operations
     */
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * Retrieves all available topics with the subscription status of the user.
     *
     * @param authentication Spring Security authentication context with user ID
     * @return a {@link ResponseEntity} of a list of {@link TopicItemResponse}
     */
    @Operation(summary = "List all topics for the current user", description = "Returns every topic with a flag indicating whether the authenticated user is subscribed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<TopicItemResponse>> getAllTopicsForUser(Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());

        List<TopicItemResponse> topicItems = topicService.findAllForUser(userId);
        return ResponseEntity.ok(topicItems);
    }

    /**
     * Retrieves a light list of all topics formatted as options (id/name),
     *
     * @return a {@link ResponseEntity} of a list of {@link TopicOptionResponse}
     */
    @Operation(summary = "List topic options", description = "Returns all topics as id/name pairs, used to populate selection fields (e.g. post form).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/options")
    public ResponseEntity<List<TopicOptionResponse>> getAllTopicsOptions() {

        List<TopicOptionResponse> topicOptions = topicService.findAllOptions();

        return ResponseEntity.ok(topicOptions);
    }

    /**
     * Retrieves only the topics the currently authenticated user is subscribed to.
     *
     * @param authentication Spring Security authentication context with user ID
     * @return a {@link ResponseEntity} of a list of {@link TopicItemResponse}
     */
    @Operation(summary = "List subscribed topics", description = "Returns only the topics the authenticated user is currently subscribed to.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscribed topics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/subscriptions")
    public ResponseEntity<List<TopicItemResponse>> getSubscribedTopics(Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());

        List<TopicItemResponse> subscribedTopics = topicService.findAllSubscribedByUser(userId);
        return ResponseEntity.ok(subscribedTopics);
    }

    /**
     * Subscribes the current user to the given topic.
     *
     * @param id             the id of the topic to subscribe
     * @param authentication Spring Security authentication context with user ID
     */
    @Operation(summary = "Subscribe to a topic", description = "Subscribes the authenticated user to the topic identified by its id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subscription successful"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User or topic not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{topicId}/subscribe")
    public ResponseEntity<Void> subscribe(@PathVariable Long topicId, Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());

        topicService.subscribe(userId, topicId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Unsubscribes the current user from the given topic.
     *
     * @param id             the id of the topic to unsubscribe
     * @param authentication Spring Security authentication context with user ID
     */
    @Operation(summary = "Unsubscribe from a topic", description = "Unsubscribes the authenticated user from the topic identified by its id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Unsubscription successful"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User or topic not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{topicId}/subscribe")
    public ResponseEntity<Void> unsubscribe(@PathVariable Long topicId, Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());

        topicService.unsubscribe(userId, topicId);
        return ResponseEntity.noContent().build();
    }
}