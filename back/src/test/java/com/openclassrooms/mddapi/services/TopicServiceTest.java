package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import com.openclassrooms.mddapi.dto.TopicItemResponse;
import com.openclassrooms.mddapi.dto.TopicOptionResponse;
import com.openclassrooms.mddapi.mappers.TopicMapper;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("TopicService")
public class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TopicMapper topicMapper;

    @InjectMocks
    private TopicService topicService;

    @Nested
    @Tag("findAllOptions")
    @DisplayName("Find all option")
    class FindAllOptionsTests {
        @Test
        @DisplayName("should return all topics formatted as options")
        void findAllOptions_shouldReturnAllTopicsOptions() {
            // GIVEN
            Long topic1Id = 1L;
            Long topic2Id = 2L;

            TopicEntity topic1 = new TopicEntity("Java", "Java ecosystem");
            topic1.setId(topic1Id);

            TopicEntity topic2 = new TopicEntity("Angular", "Angular framework");
            topic2.setId(topic2Id);

            when(topicRepository.findAll()).thenReturn(List.of(topic1, topic2));
            when(topicMapper.toOptionDTO(topic1))
                    .thenReturn(new TopicOptionResponse(topic1Id, "Java"));

            when(topicMapper.toOptionDTO(topic2))
                    .thenReturn(new TopicOptionResponse(topic2Id, "Angular"));

            // WHEN
            List<TopicOptionResponse> result = topicService.findAllOptions();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(TopicOptionResponse::id).containsExactly(topic1Id, topic2Id);
            assertThat(result).extracting(TopicOptionResponse::name).containsExactly("Java", "Angular");
        }
    }

    @Nested
    @Tag("findAllForUser")
    @DisplayName("Find all for user")
    class FindAllForUserTests {
        @Test
        @DisplayName("should return all topics with correct subscription status when user exists")
        void findAllForUser_shouldReturnAllTopicsWithSubscriptionStatus_whenUserExists() {
            // GIVEN
            Long userId = 1L;
            Long topic1Id = 2L;
            Long topic2Id = 3L;

            TopicEntity topic1 = new TopicEntity("Java", "Java ecosystem");
            topic1.setId(topic1Id);

            TopicEntity topic2 = new TopicEntity("Angular", "Angular framework");
            topic2.setId(topic2Id);

            UserEntity user = new UserEntity("john.doe@example.com", "jeanbiche", "Password123!");
            user.setId(userId);
            user.setTopics(List.of(topic1));

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.of(user));
            when(topicRepository.findAll()).thenReturn(List.of(topic1, topic2));

            when(topicMapper.toItemDTO(topic1, true))
                    .thenReturn(new TopicItemResponse(topic1Id, "Java", "Java ecosystem", true));

            when(topicMapper.toItemDTO(topic2, false))
                    .thenReturn(new TopicItemResponse(topic2Id, "Angular", "Angular framework", false));

            // WHEN
            List<TopicItemResponse> result = topicService.findAllForUser(userId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(TopicItemResponse::isSubscribed).containsExactly(true, false);
            assertThat(result).extracting(TopicItemResponse::name).containsExactly("Java", "Angular");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user is not found")
        void findAllForUser_shouldThrowException_whenUserNotFound() {
            // GIVEN
            Long userId = 99L;
            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> topicService.findAllForUser(userId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found with id: " + userId);
        }
    }

    @Nested
    @Tag("findAllSubscribedByUser")
    @DisplayName("Find all subscribed by user")
    class FindAllSubscribedByUserTests {

        @Test
        @DisplayName("should return only subscribed topics for a user")
        void findAllSubscribedByUser_shouldReturnOnlySubscribedTopics() {
            // GIVEN
            Long userId = 1L;
            Long topic1Id = 2L;
            Long topic2Id = 3L;

            TopicEntity topic1 = new TopicEntity("Java", "Java ecosystem");
            topic1.setId(topic1Id);

            TopicEntity topic2 = new TopicEntity("Angular", "Angular framework");
            topic2.setId(topic2Id);

            UserEntity user = new UserEntity("john.doe@example.com", "jeanbiche", "Password123!");
            user.setId(userId);
            user.setTopics(List.of(topic1));

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.of(user));
            when(topicMapper.toItemDTO(topic1, true))
                    .thenReturn(new TopicItemResponse(topic1Id, "Java", "Java ecosystem", true));

            // WHEN
            List<TopicItemResponse> result = topicService.findAllSubscribedByUser(userId);

            // THEN
            assertThat(result).hasSize(1);
            assertThat(result).extracting(TopicItemResponse::id).containsExactly(topic1Id);
            assertThat(result).extracting(TopicItemResponse::isSubscribed).containsOnly(true);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user is not found")
        void findAllSubscribedByUser_shouldThrowException_whenUserNotFound() {
            // GIVEN
            Long userId = 99L;
            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> topicService.findAllSubscribedByUser(userId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found with id: " + userId);
        }
    }

    @Nested
    @Tag("subscribe")
    @DisplayName("Subscribe")
    class SubscribeTests {

        @Test
        @DisplayName("should add topic to user subscriptions when not already subscribed")
        void subscribe_shouldAddTopic_whenNotAlreadySubscribed() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            TopicEntity topic = new TopicEntity("Java", "Java ecosystem");
            topic.setId(topicId);

            UserEntity user = new UserEntity("john.doe@example.com", "jeanbiche", "Password123!");
            user.setId(userId);
            user.setTopics(new ArrayList<TopicEntity>());

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.of(user));
            when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));

            // WHEN
            topicService.subscribe(userId, topicId);

            // THEN
            assertThat(user.getTopics()).contains(topic);
        }

        @Test
        @DisplayName("should do nothing when user is already subscribed")
        void subscribe_shouldDoNothing_whenAlreadySubscribed() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            TopicEntity topic = new TopicEntity("Java", "Java ecosystem");
            topic.setId(topicId);

            UserEntity user = new UserEntity("john.doe@example.com", "jeanbiche", "Password123!");
            user.setId(userId);
            user.setTopics(new ArrayList<TopicEntity>(List.of(topic)));

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.of(user));
            when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));

            // WHEN
            topicService.subscribe(userId, topicId);

            // THEN
            assertThat(user.getTopics()).hasSize(1);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user is not found")
        void subscribe_shouldThrowException_whenUserNotFound() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> topicService.subscribe(userId, topicId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found with id: " + userId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when topic is not found")
        void subscribe_shouldThrowException_whenTopicNotFound() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            UserEntity user = new UserEntity("john.doe@example.com", "jeanbiche", "Password123!");
            user.setId(userId);

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.of(user));
            when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> topicService.subscribe(userId, topicId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Topic not found with id: " + topicId);
        }
    }

    @Nested
    @DisplayName("unsubscribe")
    class UnsubscribeTests {

        @Test
        @DisplayName("should remove topic from user subscriptions")
        void unsubscribe_shouldRemoveTopic_whenSubscribed() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            TopicEntity topic = new TopicEntity("Java", "Java ecosystem");
            topic.setId(topicId);

            UserEntity user = new UserEntity("john.doe@example.com", "jeanbiche", "Password123!");
            user.setId(userId);
            user.setTopics(new ArrayList<TopicEntity>(List.of(topic)));

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.of(user));
            when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));

            // WHEN
            topicService.unsubscribe(userId, topicId);

            // THEN
            assertThat(user.getTopics()).doesNotContain(topic);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user is not found")
        void unsubscribe_shouldThrowException_whenUserNotFound() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> topicService.unsubscribe(userId, topicId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found with id: " + userId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when topic is not found")
        void unsubscribe_shouldThrowException_whenTopicNotFound() {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;

            UserEntity user = new UserEntity("john.doe@example.com", "jeanbiche", "Password123!");
            user.setId(userId);

            when(userRepository.findWithTopicsById(userId)).thenReturn(Optional.of(user));
            when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

            // THEN
            assertThatThrownBy(() -> topicService.unsubscribe(userId, topicId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Topic not found with id: " + topicId);
        }
    }
}
