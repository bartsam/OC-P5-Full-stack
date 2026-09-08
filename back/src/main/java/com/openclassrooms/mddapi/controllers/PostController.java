package com.openclassrooms.mddapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.PostCreateRequest;
import com.openclassrooms.mddapi.dto.PostDetailResponse;
import com.openclassrooms.mddapi.dto.PostItemResponse;
import com.openclassrooms.mddapi.mappers.PostMapper;
import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.services.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    /**
     * Constructs the PostController with the required dependencies.
     *
     * @param postService the service responsible for managing posts
     * @param postMapper  mapper for converting PostEntity objects to DTOs
     */
    public PostController(PostService postService, PostMapper postMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
    }

    /**
     * Creates a new post linked to the authenticated user.
     *
     * @param request        the post request with title, content and topic ID
     * @param authentication the current authentication
     * @return the created post as a {@link PostDetailResponse}
     */
    @Operation(summary = "Create a new post", description = "Creates a post linked to the authenticated user and a topic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "User or topic not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());

        PostEntity createdPost = postService.create(
                userId,
                request.title(),
                request.content(),
                request.topicId());

        return ResponseEntity.status(201).body(postMapper.toDetailResponse(createdPost));
    }

    /**
     * Retrieves all posts as light item responses, sorted by creation date.
     *
     * @param sort the sort direction: defaults to "desc" if not provided.
     * @return a {@link ResponseEntity} of a list of {@link PostItemResponse}
     */
    @Operation(summary = "Get posts feed", description = "Returns all posts as light items, sorted by creation date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<PostItemResponse>> getFeed(
            @RequestParam(defaultValue = "desc") String sort) {
        List<PostItemResponse> posts = postService.findAllFeed(sort);
        return ResponseEntity.ok(posts);
    }

    /**
     * Retrieves a single post by its ID with full details.
     *
     * @param postId the ID of the post to retrieve
     * @return a {@link ResponseEntity} of a {@link PostDetailResponse}
     */
    @Operation(summary = "Get post by ID", description = "Returns a single post with full details (author, topic, title, content)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPost(
            @PathVariable Long postId) {
        PostDetailResponse post = postService.findById(postId);
        return ResponseEntity.ok(post);
    }
}