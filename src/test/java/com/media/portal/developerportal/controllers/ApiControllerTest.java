package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.controllers.dto.ApiCreateRequest;
import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.ApiStatus;
import com.media.portal.developerportal.services.ApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;

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
        var request = new ApiCreateRequest("Scopus API", "/scopus/v1", "Search Team", null);

        var savedApi = new Api();
        savedApi.setName("Scopus API");
        savedApi.setBasePath("/scopus/v1");
        when(apiService.createApi(any(Api.class))).thenReturn(savedApi);

        var response = apiController.createApi(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

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

    @Test
    void deprecateApi_returns200_whenServiceSucceeds() {
        var response = apiController.deprecateApi(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(apiService).deprecateApi(1L);
    }

    @Test
    void listApis_returns200WithMappedResponses() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        api.setOwnerTeam("Search Team");
        Pageable pageable = Pageable.unpaged();
        Page<Api> page = new PageImpl<>(List.of(api));
        when(apiService.listApis(ApiStatus.PUBLISHED, pageable)).thenReturn(page);

        var response = apiController.listApis(ApiStatus.PUBLISHED, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Scopus API");
    }

    @Test
    void getApi_returns200WithMappedResponse() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        api.setOwnerTeam("Search Team");
        when(apiService.getApi(1L)).thenReturn(api);

        var response = apiController.getApi(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Scopus API");
        assertThat(response.getBody().basePath()).isEqualTo("/scopus/v1");
    }
}
