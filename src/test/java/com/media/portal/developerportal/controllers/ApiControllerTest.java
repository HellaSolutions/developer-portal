package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.services.ApiService;
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
class ApiControllerTest {

    @Mock
    private ApiService apiService;

    @InjectMocks
    private ApiController apiController;

    @Test
    void createApi_returns201WithSavedId_whenServiceSucceeds() {
        var requestApi = new Api();
        requestApi.setName("Scopus API");
        requestApi.setBasePath("/scopus/v1");

        var savedApi = new Api();
        savedApi.setName("Scopus API");
        savedApi.setBasePath("/scopus/v1");
        when(apiService.createApi(requestApi)).thenReturn(savedApi);

        var response = apiController.createApi(requestApi);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(savedApi.getId());
        verify(apiService).createApi(requestApi);
    }

    @Test
    void publishApi_returns200WithPublishedApi_whenServiceSucceeds() {
        var api = new Api();
        when(apiService.publishApi(1L)).thenReturn(api);

        var response = apiController.publishApi(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(apiService).publishApi(1L);
    }
}
