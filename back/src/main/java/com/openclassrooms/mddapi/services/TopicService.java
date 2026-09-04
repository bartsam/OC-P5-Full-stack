package com.openclassrooms.mddapi.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.dto.TopicItemResponse;
import com.openclassrooms.mddapi.dto.TopicOptionResponse;
import com.openclassrooms.mddapi.mappers.TopicMapper;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final TopicMapper topicMapper;

    /**
     * Constructs the TopicService with required repositories and mappers.
     *
     * @param topicRepository the repository for managing TopicEntity persistence
     * @param userRepository  the repository for managing UserEntity persistence
     * @param topicMapper     the mapper for converting Topic entities to DTOs
     */
    public TopicService(TopicRepository topicRepository, UserRepository userRepository, TopicMapper topicMapper) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.topicMapper = topicMapper;
    }

    /**
     * Retrieves all topics as light option responses.
     *
     * @return a list of TopicOptionResponse
     */
    public List<TopicOptionResponse> findAllOptions() {
        return topicRepository.findAll()
                .stream()
                .map(topicMapper::toOptionDTO)
                .toList();
    }

    /**
     * Retrieves all topics along with the subscription status for a given user.
     *
     * @param userId the ID of the user to check subscription status
     * @return a list of TopicItemResponse indicating whether the user is subscribed
     * @throws EntityNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public List<TopicItemResponse> findAllForUser(Long userId) {

        List<TopicEntity> allTopics = topicRepository.findAll();

        UserEntity user = userRepository.findWithTopicsById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return allTopics.stream()
                .map(topic -> {
                    boolean isSubscribed = user.getTopics().stream()
                            .anyMatch(t -> t.getId().equals(topic.getId()));
                    return topicMapper.toItemDTO(topic, isSubscribed);
                })
                .toList();
    }

    /**
     * Retrieves the list of topics to which a specific user is subscribed.
     *
     * @param userId the ID of the user whose subscriptions to retrieve
     * @return a list of TopicItemResponse representing the user's subscribed topics
     * @throws EntityNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public List<TopicItemResponse> findAllSubscribedByUser(Long userId) {
        UserEntity user = userRepository.findWithTopicsById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return user.getTopics().stream()
                .map(topic -> topicMapper.toItemDTO(topic, true))
                .toList();
    }

    /**
     * Subscribes a user to a specific topic.
     *
     * @param userId  the ID of the user subscribing
     * @param topicId the ID of the topic to subscribe to
     * @throws EntityNotFoundException if either the user or the topic is not found
     */
    @Transactional
    public void subscribe(Long userId, Long topicId) {
        UserEntity user = userRepository.findWithTopicsById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + topicId));

        if (!user.getTopics().contains(topic)) {
            user.getTopics().add(topic);
            userRepository.save(user);
        }
    }

    /**
     * Unsubscribes a user from a specific topic.
     *
     * @param userId  the ID of the user unsubscribing
     * @param topicId the ID of the topic to unsubscribe from
     * @throws EntityNotFoundException if either the user or the topic is not found
     */
    @Transactional
    public void unsubscribe(Long userId, Long topicId) {
        UserEntity user = userRepository.findWithTopicsById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + topicId));

        user.getTopics().remove(topic);
        userRepository.save(user);
    }
}