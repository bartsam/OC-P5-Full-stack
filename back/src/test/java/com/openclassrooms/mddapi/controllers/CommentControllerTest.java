package com.openclassrooms.mddapi.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.openclassrooms.mddapi.dto.CommentCreateRequest;
import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.mappers.CommentMapper;
import com.openclassrooms.mddapi.models.CommentEntity;
import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.services.CommentService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("CommentController")
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentController commentController;

    @Nested
    @Tag("createComment")
    @DisplayName("POST /posts/{postId}/comments")
    class CreateCommentTests {

        @Test
        @DisplayName("should return 201 with created comment response")
        void createComment_shouldReturn201WithCommentResponse() {
            // GIVEN
            Long userId = 1L;
            Long postId = 2L;

            UserEntity author = new UserEntity("john@example.com", "john", "Password123!");
            author.setId(userId);
            TopicEntity topic = new TopicEntity("Java", "Java ecosystem");

            PostEntity post = new PostEntity("Spring Boot", "Java framework", author, topic);
            post.setId(postId);

            CommentEntity comment = new CommentEntity("Great article!", author, post);
            comment.setId(3L);
            comment.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

            CommentResponse response = new CommentResponse(
                    3L,
                    "john",
                    "Great article!",
                    comment.getCreatedAt());

            CommentCreateRequest request = new CommentCreateRequest("Great article!");

            when(authentication.getName()).thenReturn(String.valueOf(userId));
            when(commentService.create(userId, postId, request.content())).thenReturn(comment);
            when(commentMapper.toResponse(comment)).thenReturn(response);

            // WHEN
            ResponseEntity<CommentResponse> result = commentController.createComment(
                    postId,
                    request,
                    authentication);

            // THEN
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isEqualTo(response);
        }
    }

    @Nested
    @Tag("getComments")
    @DisplayName("GET /posts/{postId}/comments")
    class GetCommentsTests {

        @Test
        @DisplayName("should return 200 with list of comment responses")
        void getComments_shouldReturn200WithCommentResponses() {
            // GIVEN
            Long postId = 1L;

            UserEntity author = new UserEntity("john@example.com", "john", "Password123!");
            author.setId(1L);
            TopicEntity topic = new TopicEntity("Java", "Java ecosystem");

            PostEntity post = new PostEntity("Spring Boot", "Java framework", author, topic);
            post.setId(postId);

            CommentEntity comment1 = new CommentEntity("First", author, post);
            comment1.setId(1L);
            comment1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

            CommentEntity comment2 = new CommentEntity("Second", author, post);
            comment2.setId(2L);
            comment2.setCreatedAt(LocalDateTime.of(2025, 1, 2, 10, 0));

            CommentResponse response1 = new CommentResponse(1L, "john", "First", comment1.getCreatedAt());
            CommentResponse response2 = new CommentResponse(2L, "john", "Second", comment2.getCreatedAt());

            when(commentService.findByPostId(postId)).thenReturn(List.of(response1, response2));

            // WHEN
            ResponseEntity<List<CommentResponse>> result = commentController.getComments(postId);

            // THEN
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).hasSize(2);
            assertThat(result.getBody()).extracting(CommentResponse::id)
                    .containsExactly(1L, 2L);
        }
    }
}
