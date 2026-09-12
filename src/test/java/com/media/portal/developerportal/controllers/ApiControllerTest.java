package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.repositories.ApiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

    @Mock
    private ApiRepository apiRepository;

    @InjectMocks
    private ApiController apiController;

    @Test
    void createApi_savesAndReturns201_whenNameAndBasePathAreValid() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        when(apiRepository.save(api)).thenReturn(api);

        var response = apiController.createApi(api);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(api);
        verify(apiRepository).save(api);
    }

    @Test
    void createApi_throwsBadApiRequestException_whenBasePathDoesNotMatchPattern() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("not-a-valid-path");

        assertThrows(BadApiRequestException.class, () -> apiController.createApi(api));

        verifyNoInteractions(apiRepository);
    }
}
