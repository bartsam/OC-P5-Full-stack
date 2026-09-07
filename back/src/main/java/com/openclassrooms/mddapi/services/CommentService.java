package com.openclassrooms.mddapi.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.mappers.CommentMapper;
import com.openclassrooms.mddapi.models.CommentEntity;
import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    /**
     * Constructs the CommentService with required repositories and mappers.
     *
     * @param commentRepository for managing {@link CommentEntity} persistence
     * @param postRepository    for managing {@link PostEntity} persistence
     * @param userRepository    for managing {@link UserEntity} persistence
     * @param commentMapper     for converting Comment entities to DTOs
     */
    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    /**
     * Creates and saves a new comment entity attached to an existing user and post.
     *
     * @param userId  the ID of the author creating the comment
     * @param postId  the ID of the associated post
     * @param content the text content of the comment
     * @return the saved {@link CommentEntity}
     * @throws EntityNotFoundException if the user or the post is not found
     */
    @Transactional
    public CommentEntity create(
            Long userId,
            Long postId,
            String content) {

        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + userId));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Post not found with id: " + postId));

        CommentEntity comment = new CommentEntity(content,
                author,
                post);

        return commentRepository.save(comment);
    }

    /**
     * Retrieves all comments for a given post, sorted by creation date ascending.
     *
     * @param postId the ID of the post
     * @return a list of CommentResponse
     * @throws EntityNotFoundException if the post is not found
     */
    public List<CommentResponse> findByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found with id: " + postId);
        }

        return commentRepository.findByPostId(postId, Sort.by(CommentEntity::getCreatedAt).ascending())
                .stream()
                .map(commentMapper::toResponse)
                .toList();
    }

}
