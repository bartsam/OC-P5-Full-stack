package com.openclassrooms.mddapi.controllers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.openclassrooms.mddapi.dto.TopicItemResponse;
import com.openclassrooms.mddapi.dto.TopicOptionResponse;
import com.openclassrooms.mddapi.services.TopicService;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(TopicController.class)
@Tag("unit")
@DisplayName("TopicController")
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TopicService topicService;

    @Nested
    @Tag("getAllTopicsForUser")
    @DisplayName("GET /api/topics")
    class GetAllTopicsForUserTests {

        @Test
        @DisplayName("should return 200 with topics and subscription status for the authenticated user")
        void getAllTopicsForUser_shouldReturnAllTopics_withSubscriptionStatus() throws Exception {
            // GIVEN
            Long userId = 1L;

            List<TopicItemResponse> topics = List.of(
                    new TopicItemResponse(1L, "Java", "Java ecosystem", true),
                    new TopicItemResponse(2L, "Angular", "Angular framework", false));

            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            when(topicService.findAllForUser(userId)).thenReturn(topics);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics")
                    .principal(authentication));

            // THEN
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Java"))
                    .andExpect(jsonPath("$[0].isSubscribed").value(true))
                    .andExpect(jsonPath("$[1].isSubscribed").value(false));
        }

        @Test
        @DisplayName("should return 404 when authenticated user is not found")
        void getAllTopicsForUser_shouldReturn404_whenUserNotFound() throws Exception {
            // GIVEN
            Long userId = 99L;
            Authentication authentication = new UsernamePasswordAuthenticationToken("99", null, List.of());

            when(topicService.findAllForUser(userId))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + userId));

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics")
                    .principal(authentication));

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
        void getAllTopicsOptions_shouldReturnAllTopicsOptions() throws Exception {
            // GIVEN
            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            List<TopicOptionResponse> options = List.of(
                    new TopicOptionResponse(1L, "Java"),
                    new TopicOptionResponse(2L, "Angular"));

            when(topicService.findAllOptions()).thenReturn(options);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics/options")
                    .principal(authentication));

            // THEN
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Java"))
                    .andExpect(jsonPath("$[1].name").value("Angular"));
        }

    }

    @Nested
    @Tag("getSubscribedTopics")
    @DisplayName("GET /api/topics/subscriptions")
    class GetSubscribedTopicsTests {

        @Test
        @DisplayName("should return 200 with only subscribed topics")
        void getSubscribedTopics_shouldReturnOnlySubscribedTopics() throws Exception {
            // GIVEN
            Long userId = 1L;
            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            List<TopicItemResponse> subscribed = List.of(
                    new TopicItemResponse(1L, "Java", "Java ecosystem", true));

            when(topicService.findAllSubscribedByUser(userId)).thenReturn(subscribed);

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics/subscriptions")
                    .principal(authentication));

            // THEN
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].isSubscribed").value(true));
        }

        @Test
        @DisplayName("should return 404 when authenticated user is not found")
        void getSubscribedTopics_shouldReturn404_whenUserNotFound() throws Exception {
            // GIVEN
            Long userId = 99L;
            Authentication authentication = new UsernamePasswordAuthenticationToken("99", null, List.of());

            when(topicService.findAllSubscribedByUser(userId))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + userId));

            // WHEN
            ResultActions result = mockMvc.perform(get("/api/topics/subscriptions")
                    .principal(authentication));

            // THEN
            result.andExpect(status().isNotFound());
        }

    }

    @Nested
    @Tag("subscribe")
    @DisplayName("POST /api/topics/{topicId}/subscribe")
    class SubscribeTests {

        @Test
        @DisplayName("should return 204 and call service when subscription succeeds")
        void subscribe_shouldReturn204_whenSubscriptionSucceeds() throws Exception {
            // GIVEN
            Long userId = 1L;
            Long topicId = 2L;
            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/topics/{topicId}/subscribe", topicId)
                    .principal(authentication));

            // THEN
            result.andExpect(status().isNoContent());

            verify(topicService).subscribe(userId, topicId);
        }

        @Test
        @DisplayName("should return 404 when user or topic is not found")
        void subscribe_shouldReturn404_whenUserOrTopicNotFound() throws Exception {
            // GIVEN
            Long userId = 1L;
            Long topicId = 99L;
            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            doThrow(new EntityNotFoundException("Topic not found with id: " + topicId))
                    .when(topicService)
                    .subscribe(userId, topicId);

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/topics/{topicId}/subscribe", topicId)
                    .principal(authentication));

            // THEN
            result.andExpect(status().isNotFound());
        }

    }

    @Nested
    @Tag("unsubscribe")
    @DisplayName("DELETE /api/topics/{topicId}/subscribe")
    class UnsubscribeTests {

        @Test
        @DisplayName("should return 204 and call service when unsubscription succeeds")
        // GIVEN
        void unsubscribe_shouldReturn204_whenUnsubscriptionSucceeds() throws Exception {
            Long userId = 1L;
            Long topicId = 2L;
            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            // WHEN
            ResultActions result = mockMvc.perform(delete("/api/topics/{topicId}/subscribe", topicId)
                    .principal(authentication));

            // THEN
            result.andExpect(status().isNoContent());

            verify(topicService).unsubscribe(userId, topicId);
        }

        @Test
        @DisplayName("should return 404 when user or topic is not found")
        void unsubscribe_shouldReturn404_whenUserOrTopicNotFound() throws Exception {
            // GIVEN
            Long userId = 1L;
            Long topicId = 99L;
            Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, List.of());

            doThrow(new EntityNotFoundException("Topic not found with id: " + topicId))
                    .when(topicService)
                    .unsubscribe(userId, topicId);

            // WHEN
            ResultActions result = mockMvc.perform(delete("/api/topics/{topicId}/subscribe", topicId)
                    .principal(authentication));

            // THEN
            result.andExpect(status().isNotFound());
        }

    }

}
