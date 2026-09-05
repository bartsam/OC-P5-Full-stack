package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.openclassrooms.mddapi.dto.PostDetailResponse;
import com.openclassrooms.mddapi.dto.PostItemResponse;
import com.openclassrooms.mddapi.mappers.PostMapper;
import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("PostService")
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    @Nested
    @Tag("create")
    @DisplayName("Create Post")
    class CreateTests {
        @Test
        @DisplayName("should create a post with its author and topic")
        void create_shouldSavePostWithAuthorAndTopic() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            UserEntity user = new UserEntity(
                    "john.doe@example.com",
                    "john",
                    "Password123!");
            user.setId(userId);

            TopicEntity topic = new TopicEntity(
                    "Java",
                    "Java ecosystem");
            topic.setId(topicId);

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(topicRepository.findById(topicId))
                    .thenReturn(Optional.of(topic));

            when(postRepository.save(any(PostEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            PostEntity result = postService.create(
                    userId,
                    "Spring Boot",
                    "Java framework",
                    topicId);

            // THEN
            assertThat(result.getTitle())
                    .isEqualTo("Spring Boot");

            assertThat(result.getContent())
                    .isEqualTo("Java framework");

            assertThat(result.getAuthor())
                    .isSameAs(user);

            assertThat(result.getTopic())
                    .isSameAs(topic);

            verify(postRepository).save(any(PostEntity.class));
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when author is not found")
        void create_shouldThrowException_whenAuthorNotFound() {
            // GIVEN
            Long userId = 99L;
            Long topicId = 2L;

            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> postService.create(
                    userId,
                    "Titre",
                    "Contenu",
                    topicId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found with id: " + userId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when topic is not found")
        void create_shouldThrowException_whenTopicNotFound() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 99L;

            UserEntity user = new UserEntity(
                    "john.doe@example.com",
                    "john",
                    "Password123!");
            user.setId(userId);

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            when(topicRepository.findById(topicId))
                    .thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> postService.create(
                    userId,
                    "Titre",
                    "Contenu",
                    topicId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Topic not found with id: " + topicId);
        }
    }

    @Nested
    @Tag("findAllFeed")
    @DisplayName("Find all feed")
    class FindAllFeedTests {

        @Test
        @DisplayName("should return all posts as item responses sorted descending by default")
        void findAllFeed_shouldReturnAllPostItemsSortedDesc() {
            // GIVEN
            LocalDateTime older = LocalDateTime.of(2025, 1, 1, 10, 0);
            LocalDateTime newer = LocalDateTime.of(2025, 1, 2, 10, 0);

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
            post1.setCreatedAt(older);

            PostEntity post2 = new PostEntity(
                    "New post",
                    "New content",
                    author,
                    topic);
            post2.setId(2L);
            post2.setCreatedAt(newer);

            PostItemResponse item1 = new PostItemResponse(
                    1L,
                    "Old post",
                    "Old content",
                    older);

            PostItemResponse item2 = new PostItemResponse(
                    2L,
                    "New post",
                    "New content",
                    newer);

            when(postRepository.findAllBy(
                    Sort.by(PostEntity::getCreatedAt).descending()))
                    .thenReturn(List.of(post2, post1));

            when(postMapper.toItemResponse(post1)).thenReturn(item1);
            when(postMapper.toItemResponse(post2)).thenReturn(item2);

            // WHEN
            List<PostItemResponse> result = postService.findAllFeed("desc");

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result).extracting(PostItemResponse::id)
                    .containsExactly(2L, 1L);
            assertThat(result).extracting(PostItemResponse::createdAt)
                    .containsExactly(newer, older);
        }

        @Test
        @DisplayName("should return all posts as item responses sorted ascending")
        void findAllFeed_shouldReturnAllPostItemsSortedAsc() {
            // GIVEN
            LocalDateTime older = LocalDateTime.of(2025, 1, 1, 10, 0);
            LocalDateTime newer = LocalDateTime.of(2025, 1, 2, 10, 0);

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
            post1.setCreatedAt(older);

            PostEntity post2 = new PostEntity(
                    "New post",
                    "New content",
                    author,
                    topic);
            post2.setId(2L);
            post2.setCreatedAt(newer);

            PostItemResponse item1 = new PostItemResponse(
                    1L,
                    "Old post",
                    "Old content",
                    older);

            PostItemResponse item2 = new PostItemResponse(
                    2L,
                    "New post",
                    "New content",
                    newer);

            when(postRepository.findAllBy(
                    org.springframework.data.domain.Sort.by(PostEntity::getCreatedAt).ascending()))
                    .thenReturn(List.of(post1, post2));

            when(postMapper.toItemResponse(post1)).thenReturn(item1);
            when(postMapper.toItemResponse(post2)).thenReturn(item2);

            // WHEN
            List<PostItemResponse> result = postService.findAllFeed("asc");

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result).extracting(PostItemResponse::id)
                    .containsExactly(1L, 2L);
            assertThat(result).extracting(PostItemResponse::createdAt)
                    .containsExactly(older, newer);
        }
    }

    @Nested
    @Tag("findById")
    @DisplayName("Find detailed post")
    class FindByIdTests {

        @Test
        @DisplayName("should return detailed post response when post exists")
        void findById_shouldReturnPostDetailResponse_whenPostExists() {
            // GIVEN
            Long postId = 1L;

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

            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            when(postMapper.toDetailResponse(post))
                    .thenReturn(detailResponse);

            // WHEN
            PostDetailResponse result = postService.findById(postId);

            // THEN
            assertThat(result.id()).isEqualTo(postId);
            assertThat(result.author()).isEqualTo("john");
            assertThat(result.topic()).isEqualTo("Java");
            assertThat(result.title()).isEqualTo("Spring Boot");
            assertThat(result.content()).isEqualTo("Java framework");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post is not found")
        void findById_shouldThrowException_whenPostNotFound() {
            // GIVEN
            Long postId = 99L;

            when(postRepository.findById(postId))
                    .thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> postService.findById(postId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Post not found with id: " + postId);
        }
    }
}