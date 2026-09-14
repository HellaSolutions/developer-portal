package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.controllers.dto.IntrospectRequest;
import com.media.portal.developerportal.model.SubscriptionType;
import com.media.portal.developerportal.services.Introspection;
import com.media.portal.developerportal.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntrospectControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private IntrospectController introspectController;

    @Test
    void introspect_returns200WithServiceResult_whenKeyActive() {
        var request = new IntrospectRequest("pk_someRawKey");
        var introspection = new Introspection(true, 1L, 2L, "/scopus/v1", SubscriptionType.STANDARD);
        when(subscriptionService.introspect("pk_someRawKey")).thenReturn(introspection);

        var response = introspectController.introspect(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(introspection);
    }

    @Test
    void introspect_returns200WithInactiveResult_whenKeyInvalid() {
        var request = new IntrospectRequest("pk_bogusKey");
        var inactive = new Introspection(false, null, null, null, null);
        when(subscriptionService.introspect("pk_bogusKey")).thenReturn(inactive);

        var response = introspectController.introspect(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().active()).isFalse();
    }
}
