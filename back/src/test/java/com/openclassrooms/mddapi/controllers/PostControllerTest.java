package com.openclassrooms.mddapi.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.openclassrooms.mddapi.dto.PostCreateRequest;
import com.openclassrooms.mddapi.dto.PostDetailResponse;
import com.openclassrooms.mddapi.dto.PostItemResponse;
import com.openclassrooms.mddapi.mappers.PostMapper;
import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.services.PostService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("PostController")
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private PostMapper postMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostController postController;

    @Nested
    @Tag("createPost")
    @DisplayName("POST /api/posts")
    class CreatePostTests {

        @Test
        @DisplayName("should return 201 with created post detail")
        void createPost_shouldReturn201WithPostDetail() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            UserEntity author = new UserEntity(
                    "john.doe@example.com",
                    "john",
                    "Password123!");
            author.setId(userId);

            TopicEntity topic = new TopicEntity(
                    "Java",
                    "Java ecosystem");
            topic.setId(topicId);

            PostEntity newPost = new PostEntity(
                    "Spring Boot",
                    "Java framework",
                    author,
                    topic);
            newPost.setId(10L);
            newPost.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

            PostDetailResponse detailResponse = new PostDetailResponse(
                    10L,
                    "john",
                    "Java",
                    "Spring Boot",
                    "Java framework",
                    newPost.getCreatedAt());

            PostCreateRequest request = new PostCreateRequest(
                    "Spring Boot",
                    "Java framework",
                    topicId);

            when(authentication.getName()).thenReturn(String.valueOf(userId));
            when(postService.create(anyLong(), anyString(), anyString(), anyLong()))
                    .thenReturn(newPost);
            when(postMapper.toDetailResponse(newPost))
                    .thenReturn(detailResponse);

            // WHEN
            ResponseEntity<PostDetailResponse> response = postController.createPost(
                    request,
                    authentication);

            // THEN
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(detailResponse);
        }
    }

    @Nested
    @Tag("getFeed")
    @DisplayName("GET /api/posts/")
    class GetFeedTests {

        @Test
        @DisplayName("should return 200 with list of post items")
        void getFeed_shouldReturn200WithPostItems() {
            // GIVEN
            UserEntity author = new UserEntity(
                    "john.doe@example.com",
                    "john",
                    "Password123!");
            author.setId(1L);

            TopicEntity topic = new TopicEntity(
                    "Java",
                    "Java ecosystem");
            topic.setId(2L);

            PostEntity post1 = new PostEntity(
                    "Old post",
                    "Old content",
                    author,
                    topic);
            post1.setId(1L);
            post1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

            PostEntity post2 = new PostEntity(
                    "New post",
                    "New content",
                    author,
                    topic);
            post2.setId(2L);
            post2.setCreatedAt(LocalDateTime.of(2025, 1, 2, 10, 0));

            PostItemResponse item1 = new PostItemResponse(
                    1L,
                    "Old post",
                    "Old content",
                    post1.getCreatedAt());

            PostItemResponse item2 = new PostItemResponse(
                    2L,
                    "New post",
                    "New content",
                    post2.getCreatedAt());

            when(postService.findAllFeed("desc"))
                    .thenReturn(List.of(item2, item1));

            // WHEN
            ResponseEntity<List<PostItemResponse>> response = postController.getFeed("desc");

            // THEN
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody()).extracting(PostItemResponse::id)
                    .containsExactly(2L, 1L);
        }
    }

    @Nested
    @Tag("getPost")
    @DisplayName("GET /api/posts/{postId}")
    class GetPostTests {

        @Test
        @DisplayName("should return 200 with post detail")
        void getPost_shouldReturn200WithPostDetail() {
            // GIVEN
            Long postId = 10L;

            UserEntity author = new UserEntity(
                    "john.doe@example.com",
                    "john",
                    "Password123!");
            author.setId(1L);

            TopicEntity topic = new TopicEntity(
                    "Java",
                    "Java ecosystem");
            topic.setId(2L);

            PostEntity post = new PostEntity(
                    "Spring Boot",
                    "Java framework",
                    author,
                    topic);
            post.setId(postId);
            post.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

            PostDetailResponse detailResponse = new PostDetailResponse(
                    postId,
                    "john",
                    "Java",
                    "Spring Boot",
                    "Java framework",
                    post.getCreatedAt());

            when(postService.findById(postId))
                    .thenReturn(detailResponse);

            // WHEN
            ResponseEntity<PostDetailResponse> response = postController.getPost(postId);

            // THEN
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(detailResponse);
        }
    }
}