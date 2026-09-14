package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.controllers.dto.ConsumerCreateRequest;
import com.media.portal.developerportal.controllers.dto.SubscriptionCreateRequest;
import com.media.portal.developerportal.model.Consumer;
import com.media.portal.developerportal.model.SubscriptionType;
import com.media.portal.developerportal.services.ConsumerService;
import com.media.portal.developerportal.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerControllerTest {

    @Mock
    private ConsumerService consumerService;
    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private ConsumerController consumerController;

    @Test
    void createApi_returns201WithLocationAndId() {
        var request = new ConsumerCreateRequest("Ada Lovelace", "ada@example.com", "Analytical Engines Inc");
        when(consumerService.createConsumer(any(Consumer.class))).thenReturn(42L);

        var response = consumerController.createApi(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(42L);
        assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/v1/consumers/42");

        var captor = ArgumentCaptor.forClass(Consumer.class);
        verify(consumerService).createConsumer(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Ada Lovelace");
        assertThat(captor.getValue().getEmail()).isEqualTo("ada@example.com");
        assertThat(captor.getValue().getOrganisation()).isEqualTo("Analytical Engines Inc");
    }

    @Test
    void subscribeApi_delegatesToSubscriptionServiceAndReturns201() {
        var request = new SubscriptionCreateRequest(2L, SubscriptionType.STANDARD);
        when(subscriptionService.createSubscription(1L, 2L, SubscriptionType.STANDARD)).thenReturn(9L);

        var response = consumerController.subscribeApi(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(9L);
        verify(subscriptionService).createSubscription(1L, 2L, SubscriptionType.STANDARD);
    }

    @Test
    void getConsumer_returns200WithMappedResponse() {
        var consumer = new Consumer();
        consumer.setName("Ada Lovelace");
        consumer.setEmail("ada@example.com");
        consumer.setOrganisation("Analytical Engines Inc");
        when(consumerService.getConsumer(1L)).thenReturn(consumer);

        var response = consumerController.getConsumer(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Ada Lovelace");
        assertThat(response.getBody().email()).isEqualTo("ada@example.com");
        assertThat(response.getBody().organisation()).isEqualTo("Analytical Engines Inc");
    }
}
