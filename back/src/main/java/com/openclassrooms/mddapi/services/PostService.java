package com.openclassrooms.mddapi.services;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the PostService with required repositories and mappers.
     *
     * @param postRepository  Repository for managing PostEntity persistence
     * @param userRepository  Repository for managing UserEntity persistence
     * @param topicRepository the repository for managing TopicEntity persistence
     */
    public PostService(
            PostRepository postRepository,
            TopicRepository topicRepository,
            UserRepository userRepository) {
        this.postRepository = postRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PostEntity create(
            Long userId,
            String title,
            String content,
            Long topicId) {

        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + userId));

        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Topic not found with id: " + topicId));

        PostEntity post = new PostEntity(
                title,
                content,
                author,
                topic);

        return postRepository.save(post);
    }
}
