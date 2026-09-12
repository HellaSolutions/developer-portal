package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.controllers.dto.CreateApiRequest;
import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.services.ApiService;
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
class ApiControllerTest {

    @Mock
    private ApiService apiService;

    @InjectMocks
    private ApiController apiController;

    @Test
    void createApi_returns201WithSavedId_whenServiceSucceeds() {
        var request = new CreateApiRequest("Scopus API", "/scopus/v1", "Search Team", null);

        var savedApi = new Api();
        savedApi.setName("Scopus API");
        savedApi.setBasePath("/scopus/v1");
        when(apiService.createApi(any(Api.class))).thenReturn(savedApi);

        var response = apiController.createApi(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(savedApi.getId());

        var apiCaptor = ArgumentCaptor.forClass(Api.class);
        verify(apiService).createApi(apiCaptor.capture());
        assertThat(apiCaptor.getValue().getName()).isEqualTo("Scopus API");
        assertThat(apiCaptor.getValue().getBasePath()).isEqualTo("/scopus/v1");
        assertThat(apiCaptor.getValue().getOwnerTeam()).isEqualTo("Search Team");
    }

    @Test
    void publishApi_returns200WithPublishedApi_whenServiceSucceeds() {

        var response = apiController.publishApi(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(apiService).publishApi(1L);
    }
}
