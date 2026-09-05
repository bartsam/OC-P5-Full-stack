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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.openclassrooms.mddapi.dto.PostCreateRequest;
import com.openclassrooms.mddapi.models.PostEntity;
import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
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
@DisplayName("PostController")
class PostControllerIntegrationTest {

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
    private TopicRepository topicRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private UserEntity existingUser;
    private TopicEntity topicA;
    private TopicEntity topicB;
    private String validToken;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        topicRepository.deleteAll();
        userRepository.deleteAll();

        existingUser = userRepository.save(
                new UserEntity("john.doe@example.com", "john", passwordEncoder.encode("Password123!")));

        topicA = topicRepository.save(new TopicEntity("Java", "Java ecosystem"));
        topicB = topicRepository.save(new TopicEntity("Angular", "Angular framework"));

        validToken = jwtService.generateToken(existingUser.getId());
    }

    @Nested
    @Tag("createPost")
    @DisplayName("POST /api/posts")
    class CreatePostTests {

        @Test
        @DisplayName("should return 201 and persist the post")
        void createPost_shouldReturn201AndPersistPost() throws Exception {
            // WHEN
            PostCreateRequest request = new PostCreateRequest("Spring Boot", "Java framework", topicA.getId());

            ResultActions result = mockMvc.perform(post("/api/posts")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.title").value("Spring Boot"))
                    .andExpect(jsonPath("$.content").value("Java framework"))
                    .andExpect(jsonPath("$.author").value("john"))
                    .andExpect(jsonPath("$.topic").value("Java"))
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void createPost_shouldReturn401_whenNoTokenProvided() throws Exception {
            // WHEN
            PostCreateRequest request = new PostCreateRequest("Spring Boot", "Java framework", topicA.getId());

            ResultActions result = mockMvc.perform(post("/api/posts")
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void createPost_shouldReturn400_whenRequestBodyInvalid() throws Exception {
            // WHEN
            PostCreateRequest request = new PostCreateRequest("", "", null);

            ResultActions result = mockMvc.perform(post("/api/posts")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 when topic does not exist")
        void createPost_shouldReturn404_whenTopicNotFound() throws Exception {
            // GIVEN
            PostCreateRequest request = new PostCreateRequest("Spring Boot", "Java framework", 99L);

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/posts")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .contentType("application/json")
                    .content(jsonMapper.writeValueAsString(request)));

            // THEN
            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @Tag("getFeed")
    @DisplayName("GET /api/posts")
    class GetFeedTests {

        @Test
        @DisplayName("should return 200 with list of posts sorted desc by default")
        void getFeed_shouldReturn200WithPostsSortedDesc() throws Exception {
            // GIVEN
            PostEntity post1 = new PostEntity(
                    "Old post",
                    "Old content",
                    existingUser,
                    topicA);
            post1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 1, 0));

            PostEntity post2 = new PostEntity(
                    "New post",
                    "New content",
                    existingUser,
                    topicB);
            post2.setCreatedAt(LocalDateTime.of(2026, 1, 1, 1, 0));

            postRepository.save(post1);
            postRepository.save(post2);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("New post"))
                    .andExpect(jsonPath("$[1].title").value("Old post"));
        }

        @Test
        @DisplayName("should return 200 with list of posts sorted asc when sort=asc")
        void getFeed_shouldReturn200WithPostsSortedAsc() throws Exception {
            // GIVEN
            PostEntity post1 = new PostEntity(
                    "Old post",
                    "Old content",
                    existingUser,
                    topicA);
            post1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

            PostEntity post2 = new PostEntity(
                    "New post",
                    "New content",
                    existingUser,
                    topicB);
            post2.setCreatedAt(LocalDateTime.of(2025, 1, 2, 10, 0));

            postRepository.save(post1);
            postRepository.save(post2);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .param("sort", "asc"));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("Old post"))
                    .andExpect(jsonPath("$[1].title").value("New post"));
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void getFeed_shouldReturn401_whenNoTokenProvided() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts/"));

            // THEN
            result.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @Tag("getPost")
    @DisplayName("GET /api/posts/{postId}")
    class GetPostTests {

        @Test
        @DisplayName("should return 200 with post detail")
        void getPost_shouldReturn200WithPostDetail() throws Exception {
            // GIVEN
            PostEntity post = new PostEntity(
                    "Spring Boot",
                    "Java framework",
                    existingUser,
                    topicA);
            post.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

            post = postRepository.save(post);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts/{postId}", post.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(post.getId()))
                    .andExpect(jsonPath("$.title").value("Spring Boot"))
                    .andExpect(jsonPath("$.content").value("Java framework"))
                    .andExpect(jsonPath("$.author").value("john"))
                    .andExpect(jsonPath("$.topic").value("Java"))
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void getPost_shouldReturn401_whenNoTokenProvided() throws Exception {
            // GIVEN
            PostEntity post = new PostEntity(
                    "Spring Boot",
                    "Java framework",
                    existingUser,
                    topicA);
            post = postRepository.save(post);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts/{postId}", post.getId()));

            // THEN
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when post does not exist")
        void getPost_shouldReturn404_whenPostNotFound() throws Exception {
            // GIVEN
            Long unknownPostId = 99L;

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/posts/{postId}", unknownPostId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isNotFound());
        }
    }
}