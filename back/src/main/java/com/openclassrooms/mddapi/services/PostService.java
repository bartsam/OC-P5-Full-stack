package com.openclassrooms.mddapi.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    /**
     * Constructs the PostService with required repositories and mappers.
     *
     * @param postRepository  Repository for managing PostEntity persistence
     * @param userRepository  Repository for managing UserEntity persistence
     * @param topicRepository the repository for managing TopicEntity persistence
     * @param postMapper      the mapper for converting Post entities to DTOs
     */
    public PostService(
            PostRepository postRepository,
            TopicRepository topicRepository,
            UserRepository userRepository,
            PostMapper postMapper) {
        this.postRepository = postRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.postMapper = postMapper;
    }

    /**
     * Creates and saves a new post entity attached to an existing user and topic.
     *
     * @param userId  the ID of the author creating the post
     * @param title   the title of the post
     * @param content the text content of the post
     * @param topicId the ID of the associated topic
     * @return the saved {@link PostEntity}
     * @throws EntityNotFoundException if the user or the topic is not found
     */
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

    /**
     * Retrieves posts from the topics the user is subscribed to,
     * as light item responses, sorted by creation date.
     *
     * @param userId        the ID of the user whose subscriptions are used
     * @param sortDirection "asc" or "desc"
     * @return a list of PostItemResponse
     */
    @Transactional(readOnly = true)
    public List<PostItemResponse> findFeedForUser(Long userId, String sortDirection) {
        Sort sort = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.by(PostEntity::getCreatedAt).ascending()
                : Sort.by(PostEntity::getCreatedAt).descending();

        return postRepository.findDistinctByTopic_Subscribers_Id(userId, sort)
                .stream()
                .map(postMapper::toItemResponse)
                .toList();
    }

    /**
     * Retrieves a detailed post by its ID.
     *
     * @param id the post ID
     * @return a PostDetailResponse
     * @throws EntityNotFoundException if the post is not found
     */
    public PostDetailResponse findById(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Post not found with id: " + id));

        return postMapper.toDetailResponse(post);
    }
}
