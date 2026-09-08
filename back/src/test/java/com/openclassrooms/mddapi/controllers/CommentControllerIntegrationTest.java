package com.openclassrooms.mddapi.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.openclassrooms.mddapi.dto.CommentCreateRequest;
import com.openclassrooms.mddapi.models.CommentEntity;
import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("CommentController")
class CommentControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private UserEntity existingUser;
    private TopicEntity topic;
    private PostEntity post;
    private String validToken;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        topicRepository.deleteAll();
        userRepository.deleteAll();

        existingUser = userRepository.save(
                new UserEntity("john@example.com", "john", passwordEncoder.encode("Password123!")));

        topic = topicRepository.save(new TopicEntity("Java", "Java ecosystem"));

        post = postRepository.save(
                new PostEntity("Spring Boot", "Java framework", existingUser, topic));

        validToken = jwtService.generateToken(existingUser.getId());
    }

    @Nested
    @Tag("createComment")
    @DisplayName("POST /posts/{postId}/comments")
    class CreateCommentTests {

        @Test
        @DisplayName("should return 201 and persist the comment")
        void createComment_shouldReturn201AndPersistComment_whenRequestValid() throws Exception {
            // GIVEN
            String content = "Great article!";
            CommentCreateRequest request = new CommentCreateRequest(content);

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/posts/{postId}/comments", post.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            ResultActions resultComments = mockMvc.perform(get("/api/posts/{postId}/comments", post.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value(content))
                    .andExpect(jsonPath("$.author").value("john"));

            resultComments.andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].content").value(content));
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void createComment_shouldReturn401_whenNoTokenProvided() throws Exception {
            // GIVEN
            CommentCreateRequest request = new CommentCreateRequest("Test");

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/posts/{postId}/comments", post.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when post does not exist")
        void createComment_shouldReturn404_whenPostNotFound() throws Exception {
            // GIVEN
            Long unknownPostId = 99L;
            CommentCreateRequest request = new CommentCreateRequest("Test");

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/posts/{postId}/comments", unknownPostId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when content is empty")
        void createComment_shouldReturn400_whenContentEmpty() throws Exception {
            // GIVEN
            CommentCreateRequest request = new CommentCreateRequest("");

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/posts/{postId}/comments", post.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @Tag("getComments")
    @DisplayName("GET /posts/{postId}/comments")
    class GetCommentsTests {

        @Test
        @DisplayName("should return 200 with all comments for the post sorted by createdAt")
        void getComments_shouldReturn200WithComments_whenPostExists() throws Exception {
            // GIVEN
            CommentEntity comment1 = new CommentEntity("First comment", existingUser, post);
            comment1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

            CommentEntity comment2 = new CommentEntity("Second comment", existingUser, post);
            comment2.setCreatedAt(LocalDateTime.of(2025, 1, 2, 10, 0));

            commentRepository.save(comment1);
            commentRepository.save(comment2);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts/{postId}/comments", post.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].content").value("First comment"))
                    .andExpect(jsonPath("$[1].content").value("Second comment"));
        }

        @Test
        @DisplayName("should return 200 with empty list when post has no comments")
        void getComments_shouldReturn200WithEmptyList_whenNoComments() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts/{postId}/comments", post.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void getComments_shouldReturn401_whenNoTokenProvided() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts/{postId}/comments", post.getId()));

            // THEN
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when post does not exist")
        void getComments_shouldReturn404_whenPostNotFound() throws Exception {
            // GIVEN
            Long unknownPostId = 99L;

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts/{postId}/comments", unknownPostId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isNotFound());
        }
    }
}