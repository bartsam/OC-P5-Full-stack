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

import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.mappers.CommentMapper;
import com.openclassrooms.mddapi.models.CommentEntity;
import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("CommentService")
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    @Nested
    @Tag("create")
    @DisplayName("Create Comment")
    class CreateTests {

        @Test
        @DisplayName("should create a comment with its author and post")
        void create_shouldSaveCommentWithAuthorAndPost() {
            // GIVEN
            Long userId = 1L;
            Long postId = 2L;

            UserEntity author = new UserEntity(
                    "john.doe@example.com",
                    "john",
                    "Password123!");
            author.setId(userId);
            TopicEntity topic = new TopicEntity("Java", "Java ecosystem");

            PostEntity post = new PostEntity(
                    "Spring Boot",
                    "Java framework",
                    author,
                    topic);
            post.setId(postId);

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(author));

            when(postRepository.findById(postId))
                    .thenReturn(Optional.of(post));

            when(commentRepository.save(any(CommentEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            CommentEntity result = commentService.create(
                    userId,
                    postId,
                    "Great article!");

            // THEN
            assertThat(result.getContent())
                    .isEqualTo("Great article!");

            assertThat(result.getAuthor())
                    .isSameAs(author);

            assertThat(result.getPost())
                    .isSameAs(post);

            verify(commentRepository).save(any(CommentEntity.class));
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when author is not found")
        void create_shouldThrowException_whenAuthorNotFound() {
            // GIVEN
            Long userId = 99L;
            Long postId = 2L;

            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> commentService.create(
                    userId,
                    postId,
                    "Comment"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found with id: " + userId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post is not found")
        void create_shouldThrowException_whenPostNotFound() {
            // GIVEN
            Long userId = 1L;
            Long postId = 99L;

            UserEntity author = new UserEntity(
                    "john.doe@example.com",
                    "john",
                    "Password123!");
            author.setId(userId);

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(author));

            when(postRepository.findById(postId))
                    .thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> commentService.create(
                    userId,
                    postId,
                    "Comment"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Post not found with id: " + postId);
        }
    }

    @Nested
    @Tag("findByPostId")
    @DisplayName("Find comments by post")
    class FindByPostIdTests {

        @Test
        @DisplayName("should return all comments for a post sorted by createdAt ascending")
        void findByPostId_shouldReturnCommentsSortedAsc() {
            // GIVEN
            Long postId = 1L;

            UserEntity author = new UserEntity(
                    "john.doe@example.com",
                    "john",
                    "Password123!");
            author.setId(1L);
            TopicEntity topic = new TopicEntity("Java", "Java ecosystem");

            PostEntity post = new PostEntity(
                    "Spring Boot",
                    "Java framework",
                    author,
                    topic);
            post.setId(postId);

            CommentEntity comment1 = new CommentEntity(
                    "First comment",
                    author,
                    post);
            comment1.setId(1L);
            comment1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 1, 0));

            CommentEntity comment2 = new CommentEntity(
                    "Second comment",
                    author,
                    post);
            comment2.setId(2L);
            comment2.setCreatedAt(LocalDateTime.of(2026, 1, 1, 1, 0));

            CommentResponse response1 = new CommentResponse(
                    1L,
                    "john",
                    "First comment",
                    comment1.getCreatedAt());

            CommentResponse response2 = new CommentResponse(
                    2L,
                    "john",
                    "Second comment",
                    comment2.getCreatedAt());

            when(postRepository.existsById(postId))
                    .thenReturn(true);

            when(commentRepository.findByPostId(postId, Sort.by(CommentEntity::getCreatedAt).ascending()))
                    .thenReturn(List.of(comment1, comment2));

            when(commentMapper.toResponse(comment1)).thenReturn(response1);
            when(commentMapper.toResponse(comment2)).thenReturn(response2);

            // WHEN
            List<CommentResponse> result = commentService.findByPostId(postId);

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result).extracting(CommentResponse::id)
                    .containsExactly(1L, 2L);
            assertThat(result).extracting(CommentResponse::createdAt)
                    .containsExactly(
                            comment1.getCreatedAt(),
                            comment2.getCreatedAt());
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post is not found")
        void findByPostId_shouldThrowException_whenPostNotFound() {
            // GIVEN
            Long postId = 99L;

            when(postRepository.existsById(postId))
                    .thenReturn(false);

            // THEN
            assertThatThrownBy(() -> commentService.findByPostId(postId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Post not found with id: " + postId);
        }
    }
}
