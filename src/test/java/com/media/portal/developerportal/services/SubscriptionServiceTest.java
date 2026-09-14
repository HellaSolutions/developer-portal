package com.media.portal.developerportal.services;

import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.ApiKey;
import com.media.portal.developerportal.model.ApiStatus;
import com.media.portal.developerportal.model.Consumer;
import com.media.portal.developerportal.model.Subscription;
import com.media.portal.developerportal.model.SubscriptionStatus;
import com.media.portal.developerportal.model.SubscriptionType;
import com.media.portal.developerportal.repositories.ApiRepository;
import com.media.portal.developerportal.repositories.ApyKeyRepository;
import com.media.portal.developerportal.repositories.ConsumerRepository;
import com.media.portal.developerportal.repositories.SubscriptionRepository;
import com.media.portal.developerportal.services.interfaces.IntrospectionView;
import com.media.portal.developerportal.utils.TokenUtil;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;
    @Mock
    private ApiRepository apiRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private ApyKeyRepository apikeyRepository;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(
                consumerRepository, apiRepository, subscriptionRepository, apikeyRepository,
                new SimpleMeterRegistry());
    }

    // ---- createSubscription ----

    @Test
    void createSubscription_savesActiveSubscription_whenApiPublishedAndNoneExists() {
        var consumer = new Consumer();
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        api.setOpenApiSpec("spec");
        api.publish();

        when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(apiRepository.findById(2L)).thenReturn(Optional.of(api));
        when(subscriptionRepository.existsByConsumerAndApiAndStatusNot(consumer, api, SubscriptionStatus.REVOKED))
                .thenReturn(false);

        var saved = mock(Subscription.class);
        when(saved.getId()).thenReturn(7L);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(saved);

        var id = subscriptionService.createSubscription(1L, 2L, SubscriptionType.STANDARD);

        assertThat(id).isEqualTo(7L);
    }

    @Test
    void createSubscription_throwsResourceNotFoundException_whenConsumerMissing() {
        when(consumerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> subscriptionService.createSubscription(1L, 2L, SubscriptionType.STANDARD));
    }

    @Test
    void createSubscription_throwsResourceNotFoundException_whenApiMissing() {
        when(consumerRepository.findById(1L)).thenReturn(Optional.of(new Consumer()));
        when(apiRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> subscriptionService.createSubscription(1L, 2L, SubscriptionType.STANDARD));
    }

    @Test
    void createSubscription_throwsBadRequestException_whenApiNotPublished() {
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1"); // still DRAFT

        when(consumerRepository.findById(1L)).thenReturn(Optional.of(new Consumer()));
        when(apiRepository.findById(2L)).thenReturn(Optional.of(api));

        assertThrows(BadRequestException.class,
                () -> subscriptionService.createSubscription(1L, 2L, SubscriptionType.STANDARD));
    }

    @Test
    void createSubscription_throwsConflictException_whenNonRevokedSubscriptionExists() {
        var consumer = new Consumer();
        var api = new Api();
        api.setName("Scopus API");
        api.setBasePath("/scopus/v1");
        api.setOpenApiSpec("spec");
        api.publish();

        when(consumerRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(apiRepository.findById(2L)).thenReturn(Optional.of(api));
        when(subscriptionRepository.existsByConsumerAndApiAndStatusNot(consumer, api, SubscriptionStatus.REVOKED))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> subscriptionService.createSubscription(1L, 2L, SubscriptionType.STANDARD));
    }

    // ---- generateKey ----

    @Test
    void generateKey_createsKey_whenActiveAndUnderLimit() {
        var subscription = mock(Subscription.class);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(subscription));
        when(apikeyRepository.findBySubscription(subscription)).thenReturn(List.of());

        var saved = mock(ApiKey.class);
        when(saved.getId()).thenReturn(99L);
        when(apikeyRepository.save(any(ApiKey.class))).thenReturn(saved);

        var response = subscriptionService.generateKey(5L);

        assertThat(response.keyId()).isEqualTo(99L);
        assertThat(response.key()).startsWith("pk_");
    }

    @Test
    void generateKey_throwsBadRequestException_whenSubscriptionNotActive() {
        var subscription = mock(Subscription.class);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.SUSPENDED);
        when(subscriptionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(subscription));

        assertThrows(BadRequestException.class, () -> subscriptionService.generateKey(5L));
    }

    @Test
    void generateKey_throwsResourceNotFoundException_whenSubscriptionMissing() {
        when(subscriptionRepository.findByIdForUpdate(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.generateKey(5L));
    }

    @Test
    void generateKey_throwsConflictException_whenTwoLiveKeysAlreadyExist() {
        var subscription = mock(Subscription.class);
        when(subscription.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(subscription));

        var liveKey1 = mock(ApiKey.class);
        when(liveKey1.getRevokedAt()).thenReturn(null);
        when(liveKey1.getExpiresAt()).thenReturn(null);
        var liveKey2 = mock(ApiKey.class);
        when(liveKey2.getRevokedAt()).thenReturn(null);
        when(liveKey2.getExpiresAt()).thenReturn(Instant.now().plus(1, ChronoUnit.DAYS));

        when(apikeyRepository.findBySubscription(subscription)).thenReturn(List.of(liveKey1, liveKey2));

        assertThrows(ConflictException.class, () -> subscriptionService.generateKey(5L));
        verify(apikeyRepository, never()).save(any());
    }

    // ---- revoke ----

    @Test
    void revoke_setsRevokedAt_whenKeyBelongsToSubscription() {
        var subscription = new Subscription();
        when(subscriptionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(subscription));

        var apiKey = mock(ApiKey.class);
        var ownerSubscription = mock(Subscription.class);
        when(ownerSubscription.getId()).thenReturn(5L);
        when(apiKey.getSubscription()).thenReturn(ownerSubscription);
        when(apiKey.getRevokedAt()).thenReturn(null);
        when(apikeyRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(apiKey));

        subscriptionService.revoke(5L, 9L);

        verify(apiKey).setRevokedAt(any(Instant.class));
    }

    @Test
    void revoke_throwsResourceNotFoundException_whenKeyBelongsToDifferentSubscription() {
        when(subscriptionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(new Subscription()));

        var apiKey = mock(ApiKey.class);
        var otherSubscription = mock(Subscription.class);
        when(otherSubscription.getId()).thenReturn(123L);
        when(apiKey.getSubscription()).thenReturn(otherSubscription);
        when(apikeyRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(apiKey));

        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.revoke(5L, 9L));
    }

    // ---- introspect ----

    @Test
    void introspect_returnsInactive_whenKeyNotFound() {
        when(apikeyRepository.findIntrospectionByKeyHash(any())).thenReturn(Optional.empty());

        var result = subscriptionService.introspect("pk_doesNotExist");

        assertThat(result.active()).isFalse();
    }

    @Test
    void introspect_returnsInactive_whenApiNotPublished() {
        var rawKey = "pk_rawKeyValue";
        var view = mock(IntrospectionView.class);
        when(view.getRevokedAt()).thenReturn(null);
        when(view.getExpiresAt()).thenReturn(null);
        when(view.getSubStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(view.getApiStatus()).thenReturn(ApiStatus.DRAFT);
        when(apikeyRepository.findIntrospectionByKeyHash(TokenUtil.sha256(rawKey))).thenReturn(Optional.of(view));

        var result = subscriptionService.introspect(rawKey);

        assertThat(result.active()).isFalse();
    }

    @Test
    void introspect_returnsInactive_whenKeyRevoked() {
        var rawKey = "pk_rawKeyValue";
        var view = mock(IntrospectionView.class);
        when(view.getRevokedAt()).thenReturn(Instant.now());
        when(view.getExpiresAt()).thenReturn(null);
        when(view.getSubStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(view.getApiStatus()).thenReturn(ApiStatus.PUBLISHED);
        when(apikeyRepository.findIntrospectionByKeyHash(TokenUtil.sha256(rawKey))).thenReturn(Optional.of(view));

        var result = subscriptionService.introspect(rawKey);

        assertThat(result.active()).isFalse();
    }

    @Test
    void introspect_returnsInactive_whenKeyExpired() {
        var rawKey = "pk_rawKeyValue";
        var view = mock(IntrospectionView.class);
        when(view.getRevokedAt()).thenReturn(null);
        when(view.getExpiresAt()).thenReturn(Instant.now().minus(1, ChronoUnit.DAYS));
        when(view.getSubStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(view.getApiStatus()).thenReturn(ApiStatus.PUBLISHED);
        when(apikeyRepository.findIntrospectionByKeyHash(TokenUtil.sha256(rawKey))).thenReturn(Optional.of(view));

        var result = subscriptionService.introspect(rawKey);

        assertThat(result.active()).isFalse();
    }

    @Test
    void introspect_returnsActive_whenKeyValidAndApiPublishedAndSubscriptionActive() {
        var rawKey = "pk_rawKeyValue";
        var view = mock(IntrospectionView.class);
        when(view.getRevokedAt()).thenReturn(null);
        when(view.getExpiresAt()).thenReturn(Instant.now().plus(1, ChronoUnit.DAYS));
        when(view.getSubStatus()).thenReturn(SubscriptionStatus.ACTIVE);
        when(view.getApiStatus()).thenReturn(ApiStatus.PUBLISHED);
        when(view.getConsumerId()).thenReturn(11L);
        when(view.getApiId()).thenReturn(22L);
        when(view.getBasePath()).thenReturn("/scopus/v1");
        when(view.getPlan()).thenReturn(SubscriptionType.PARTNER);
        when(apikeyRepository.findIntrospectionByKeyHash(TokenUtil.sha256(rawKey))).thenReturn(Optional.of(view));

        var result = subscriptionService.introspect(rawKey);

        assertThat(result.active()).isTrue();
        assertThat(result.consumerId()).isEqualTo(11L);
        assertThat(result.apiId()).isEqualTo(22L);
        assertThat(result.basePath()).isEqualTo("/scopus/v1");
        assertThat(result.plan()).isEqualTo(SubscriptionType.PARTNER);
    }
}
