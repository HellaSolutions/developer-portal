package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.model.SubscriptionType;
import com.media.portal.developerportal.services.BadRequestException;
import com.media.portal.developerportal.services.ConflictException;
import com.media.portal.developerportal.services.ConsumerService;
import com.media.portal.developerportal.services.ResourceNotFoundException;
import com.media.portal.developerportal.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full Spring MVC dispatch (real routing, path-variable binding, and
 * {@link GlobalExceptionHandler} resolution) — the two things a plain
 * Mockito-mocked {@link ConsumerControllerTest} can't observe, since it
 * calls controller methods directly in Java and bypasses the dispatcher.
 */
@WebMvcTest(ConsumerController.class)
class ConsumerControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsumerService consumerService;
    @MockitoBean
    private SubscriptionService subscriptionService;

    @Test
    void subscribeApi_bindsIdPathSegmentToConsumerIdParameter_andReturns201() throws Exception {
        when(subscriptionService.createSubscription(1L, 2L, SubscriptionType.STANDARD)).thenReturn(9L);

        mockMvc.perform(post("/v1/consumers/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apiId": 2, "plan": "STANDARD"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("9"));

        // proves the "{id}" path segment actually reached the "consumerId" parameter;
        // before the fix Spring couldn't resolve it and this call never happened.
        verify(subscriptionService).createSubscription(1L, 2L, SubscriptionType.STANDARD);
    }

    @Test
    void subscribeApi_returns400_whenServiceThrowsBadRequestException() throws Exception {
        when(subscriptionService.createSubscription(any(), any(), any()))
                .thenThrow(new BadRequestException("API is not PUBLISHED"));

        mockMvc.perform(post("/v1/consumers/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apiId": 2, "plan": "STANDARD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("API is not PUBLISHED"));
    }

    @Test
    void subscribeApi_returns404_whenServiceThrowsResourceNotFoundException() throws Exception {
        when(subscriptionService.createSubscription(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Consumer with id 1 not found"));

        mockMvc.perform(post("/v1/consumers/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apiId": 2, "plan": "STANDARD"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void subscribeApi_returns409_whenServiceThrowsConflictException() throws Exception {
        when(subscriptionService.createSubscription(any(), any(), any()))
                .thenThrow(new ConflictException("Subscription exists and has been not revoked"));

        mockMvc.perform(post("/v1/consumers/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"apiId": 2, "plan": "STANDARD"}
                                """))
                .andExpect(status().isConflict());
    }
}
