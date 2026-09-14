package com.media.portal.developerportal.services;

import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.ApiStatus;
import com.media.portal.developerportal.model.SubscriptionStatus;
import com.media.portal.developerportal.repositories.ApiRepository;
import com.media.portal.developerportal.repositories.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiServiceTest {

    @Mock
    private ApiRepository apiRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private ApiService apiService;

    @Test
    void createApi_savesAndReturnsIt() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        when(apiRepository.save(api)).thenReturn(api);

        var result = apiService.createApi(api);

        assertThat(result).isSameAs(api);
        verify(apiRepository).save(api);
    }

    @Test
    void publishApi_throwsResourceNotFoundException_whenApiDoesNotExist() {
        when(apiRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> apiService.publishApi(42L));
    }

    @Test
    void publishApi_publishesApi_whenFound() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        api.setOpenApiSpec("spec");
        when(apiRepository.findById(1L)).thenReturn(Optional.of(api));

        apiService.publishApi(1L);

        assertThat(api.getStatus()).isEqualTo(ApiStatus.PUBLISHED);
    }

    @Test
    void deprecateApi_deprecatesApiAndSuspendsActiveSubscriptions_whenFound() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        api.setOpenApiSpec("spec");
        api.publish();
        when(apiRepository.findById(1L)).thenReturn(Optional.of(api));

        apiService.deprecateApi(1L);

        assertThat(api.getStatus()).isEqualTo(ApiStatus.DEPRECATED);
        verify(subscriptionRepository).updateStatusForApi(1L, SubscriptionStatus.ACTIVE, SubscriptionStatus.SUSPENDED);
    }

    @Test
    void deprecateApi_throwsResourceNotFoundException_whenApiDoesNotExist() {
        when(apiRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> apiService.deprecateApi(42L));
        verify(subscriptionRepository, never()).updateStatusForApi(any(), any(), any());
    }

    @Test
    void listApis_delegatesToFindAllByStatus_whenStatusGiven() {
        Pageable pageable = Pageable.unpaged();
        Page<Api> page = new PageImpl<>(java.util.List.of());
        when(apiRepository.findAllByStatus(ApiStatus.PUBLISHED, pageable)).thenReturn(page);

        var result = apiService.listApis(ApiStatus.PUBLISHED, pageable);

        assertThat(result).isSameAs(page);
        verify(apiRepository, never()).findAll(pageable);
    }

    @Test
    void listApis_delegatesToFindAll_whenStatusIsNull() {
        Pageable pageable = Pageable.unpaged();
        Page<Api> page = new PageImpl<>(java.util.List.of());
        when(apiRepository.findAll(pageable)).thenReturn(page);

        var result = apiService.listApis(null, pageable);

        assertThat(result).isSameAs(page);
    }

    @Test
    void getApi_returnsApi_whenFound() {
        var api = mock(Api.class);
        when(apiRepository.findById(1L)).thenReturn(Optional.of(api));

        assertThat(apiService.getApi(1L)).isSameAs(api);
    }

    @Test
    void getApi_throwsResourceNotFoundException_whenMissing() {
        when(apiRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> apiService.getApi(42L));
    }
}
