package com.media.portal.developerportal.services;

import com.media.portal.developerportal.controllers.BadApiRequestException;
import com.media.portal.developerportal.controllers.ResourceNotFoundException;
import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.repositories.ApiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiServiceTest {

    @Mock
    private ApiRepository apiRepository;

    @InjectMocks
    private ApiService apiService;

    @Test
    void createApi_savesAndReturnsIt_whenNameAndBasePathAreValid() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        when(apiRepository.save(api)).thenReturn(api);

        var result = apiService.createApi(api);

        assertThat(result).isSameAs(api);
        verify(apiRepository).save(api);
    }

    @Test
    void createApi_throwsBadApiRequestException_whenBasePathDoesNotMatchPattern() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("not-a-valid-path");

        assertThrows(BadApiRequestException.class, () -> apiService.createApi(api));

        verifyNoInteractions(apiRepository);
    }

    @Test
    void publishApi_throwsResourceNotFoundException_whenApiDoesNotExist() {
        when(apiRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> apiService.publishApi(42L));
    }
}
