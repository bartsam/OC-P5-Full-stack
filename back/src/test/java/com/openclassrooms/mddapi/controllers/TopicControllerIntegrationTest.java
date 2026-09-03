package com.openclassrooms.mddapi.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.openclassrooms.mddapi.models.TopicEntity;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("TopicController")
class TopicControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

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
        userRepository.deleteAll();
        topicRepository.deleteAll();

        existingUser = userRepository
                .save(new UserEntity("john.doe@example.com", "jeanbiche", passwordEncoder.encode("Password123!")));

        topicA = topicRepository.save(new TopicEntity("Java", "Java ecosystem"));
        topicB = topicRepository.save(new TopicEntity("Angular", "Angular framework"));

        validToken = jwtService.generateToken(existingUser.getId());
    }

    @Nested
    @Tag("getAllTopicsForUser")
    @DisplayName("GET /api/topics")
    class GetAllTopicsForUserTests {

        @Test
        @DisplayName("should return 200 with all topics and correct subscription status")
        void getAllTopicsForUser_shouldReturn200WithStatus_whenTokenValid() throws Exception {
            // GIVEN
            existingUser.setTopics(List.of(topicA));
            userRepository.save(existingUser);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[?(@.name=='Java')].isSubscribed").value(true))
                    .andExpect(jsonPath("$[?(@.name=='Angular')].isSubscribed").value(false));
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void getAllTopicsForUser_shouldReturn401_whenNoTokenProvided() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics"));

            // THEN
            result.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should return 404 when user from token no longer exists")
        void getAllTopicsForUser_shouldReturn404_whenUserDeleted() throws Exception {
            // GIVEN
            String tokenForDeletedUser = jwtService.generateToken(existingUser.getId());
            userRepository.deleteAll();

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenForDeletedUser));

            // THEN
            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @Tag("getAllTopicsOptions")
    @DisplayName("GET /api/topics/options")
    class GetAllTopicsOptionsTests {

        @Test
        @DisplayName("should return 200 with the light list of topics")
        void getAllTopicsOptions_shouldReturn200WithTopicOptions_whenTokenValid() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics/options")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[0].description").doesNotExist());
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void getAllTopicsOptions_shouldReturn401_whenNoTokenProvided() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics/options"));

            // THEN
            result.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @Tag("getSubscribedTopics")
    @DisplayName("GET /api/topics/subscribed")
    class GetSubscribedTopicsTests {

        @Test
        @DisplayName("should return 200 with the topics subscribed by the user")
        void getSubscribedTopics_shouldReturn200WithSubscribedTopics_whenTokenValid() throws Exception {
            // GIVEN
            existingUser.setTopics(List.of(topicA));
            userRepository.save(existingUser);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics/subscribed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("Java"))
                    .andExpect(jsonPath("$[0].isSubscribed").value(true));
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void getSubscribedTopics_shouldReturn401_whenNoTokenProvided() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics/subscribed"));

            // THEN
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when user from token no longer exists")
        void getSubscribedTopics_shouldReturn404_whenUserDeleted() throws Exception {
            // GIVEN
            String tokenForDeletedUser = jwtService.generateToken(existingUser.getId());
            userRepository.deleteAll();

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics/subscribed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenForDeletedUser));

            // THEN
            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @Tag("subscribe")
    @DisplayName("POST /api/topics/{topicId}/subscribe")
    class SubscribeTests {

        @Test
        @DisplayName("should return 204 and persist the subscription")
        void subscribe_shouldReturn204AndPersistSubscription_whenRequestValid() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(post("/api/topics/{topicId}/subscribe", topicA.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            ResultActions resultSubscriptions = mockMvc.perform(get("/api/topics/subscribed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isNoContent());
            resultSubscriptions
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(topicA.getId()));
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void subscribe_shouldReturn401_whenNoTokenProvided() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(post("/api/topics/{topicId}/subscribe", topicA.getId()));

            // THEN
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when topic does not exist")
        void subscribe_shouldReturn404_whenTopicNotFound() throws Exception {
            // GIVEN
            Long unknownTopicId = 99L;

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/topics/{topicId}/subscribe", unknownTopicId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 204 and not duplicate subscription when already subscribed")
        void subscribe_shouldReturn204_whenAlreadySubscribed() throws Exception {
            // GIVEN
            existingUser.setTopics(List.of(topicA));
            userRepository.save(existingUser);

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/topics/{topicId}/subscribe", topicA.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            ResultActions resultSubscriptions = mockMvc.perform(get("/api/topics/subscribed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isNoContent());
            resultSubscriptions
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(topicA.getId()));
        }
    }

    @Nested
    @Tag("unsubscribe")
    @DisplayName("DELETE /api/topics/{topicId}/subscribe")
    class UnsubscribeTests {

        @Test
        @DisplayName("should return 204 and remove the subscription")
        void unsubscribe_shouldReturn204AndRemoveSubscription_whenRequestValid() throws Exception {
            // GIVEN
            existingUser.setTopics(new java.util.ArrayList<>(List.of(topicA, topicB)));
            userRepository.save(existingUser);

            // WHEN
            ResultActions result = mockMvc.perform(delete("/api/topics/{topicId}/subscribe", topicA.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            ResultActions resultSubscriptions = mockMvc.perform(get("/api/topics/subscribed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isNoContent());
            resultSubscriptions
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(topicB.getId()));
        }

        @Test
        @DisplayName("should return 401 when no Authorization header is provided")
        void unsubscribe_shouldReturn401_whenNoTokenProvided() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(delete("/api/topics/{topicId}/subscribe",
                    topicA.getId()));

            // THEN
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 404 when topic does not exist")
        void unsubscribe_shouldReturn404_whenTopicNotFound() throws Exception {
            // GIVEN
            Long unknownTopicId = 99L;

            // WHEN
            ResultActions result = mockMvc.perform(delete("/api/topics/{topicId}/subscribe", unknownTopicId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken));

            // THEN
            result.andExpect(status().isNotFound());
        }
    }
}