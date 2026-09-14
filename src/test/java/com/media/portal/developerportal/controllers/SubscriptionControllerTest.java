package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.controllers.dto.ApiKeyCreateResponse;
import com.media.portal.developerportal.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    @Test
    void generateKey_returns201WithKeyResponse() {
        var keyResponse = new ApiKeyCreateResponse(5L, "pk_plainKeyValue");
        when(subscriptionService.generateKey(1L)).thenReturn(keyResponse);

        var response = subscriptionController.generateKey(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(keyResponse);
    }

    @Test
    void revokeSubscription_returns200AndDelegatesToService() {
        var response = subscriptionController.revokeSubscription(1L, 9L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(subscriptionService).revoke(1L, 9L);
    }
}
