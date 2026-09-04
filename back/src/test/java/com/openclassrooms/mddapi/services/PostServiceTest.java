package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
