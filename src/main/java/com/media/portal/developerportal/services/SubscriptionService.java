package com.media.portal.developerportal.services;

import com.media.portal.developerportal.controllers.dto.ApiKeyCreateResponse;
import com.media.portal.developerportal.model.*;
import com.media.portal.developerportal.repositories.ApiRepository;
import com.media.portal.developerportal.repositories.ApyKeyRepository;
import com.media.portal.developerportal.repositories.ConsumerRepository;
import com.media.portal.developerportal.repositories.SubscriptionRepository;
import com.media.portal.developerportal.utils.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final ConsumerRepository consumerRepository;
    private final ApiRepository apiRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ApyKeyRepository apikeyRepository;

    public SubscriptionService(ConsumerRepository consumerRepository, ApiRepository apiRepository, SubscriptionRepository subscriptionRepository, ApyKeyRepository apikeyRepository) {
        this.consumerRepository = consumerRepository;
        this.apiRepository = apiRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.apikeyRepository = apikeyRepository;
    }

    @Transactional
    public Long createSubscription(Long consumerId, Long apiId, SubscriptionType plan) {

        var consumer = consumerRepository.findById(consumerId).
                orElseThrow(() -> new ResourceNotFoundException(String.format("Consumer with id %s not found", consumerId)));
        var api = apiRepository.findById(apiId).
                orElseThrow(() -> new ResourceNotFoundException(String.format("API with id %s not found", apiId)));
        if (api.getStatus() != ApiStatus.PUBLISHED) {
            throw new BadRequestException(String.format("API with id %s is not in %s", apiId, ApiStatus.PUBLISHED));
        }
        if (subscriptionRepository.existsByConsumerAndApiAndStatusNot(consumer, api, SubscriptionStatus.REVOKED)) {
                throw new ConflictException("Subscription exists and has been not revoked");
        }
        var subscription = new Subscription();
        subscription.setApi(api);
        subscription.setConsumer(consumer);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        var savedSubscription = subscriptionRepository.save(subscription);
        return savedSubscription.getId();
    }

    @Transactional
    public ApiKeyCreateResponse generateKey(Long id) {

        var subscription = subscriptionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Subscription not found, id %s", id)));
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BadRequestException(String.format("Subscription is not ACTIVE, id %s", id));
        }
        //apiKeys is not a large list
        var apiKeys = apikeyRepository.findBySubscription(subscription);
        apiKeys = apiKeys.stream().filter(k ->
                k.getRevokedAt() == null &&
                        (k.getExpiresAt() == null || k.getExpiresAt().isAfter(Instant.now()))
        ).toList();
        if (apiKeys.size() >= 2) {
            throw new ConflictException(String.format("You cannot hold more that 2 API keys, id %s", id));
        }
        var token = TokenUtil.generateToken();
        var plain = token.plainToken();
        var hashed = token.hashedToken();
        var apiKey = new ApiKey();
        apiKey.setSubscription(subscription);
        if (plain.length() != 35) {
            throw new IllegalStateException("Invalid API token, wrong length");
        }
        apiKey.setPrefix(plain.substring(0, 8));
        apiKey.setKeyHash(hashed);
        apiKey.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        var saved = apikeyRepository.save(apiKey);
        return new ApiKeyCreateResponse(saved.getId(), plain);
    }

    @Transactional
    public void revoke(Long subscriptionId, Long keyId) {

        subscriptionRepository.findByIdForUpdate(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Subscription not found, id %s", subscriptionId)));
        var apiKey = apikeyRepository.findByIdForUpdate(keyId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("API key not found, id %s", keyId)));
        if (!apiKey.getSubscription().getId().equals(subscriptionId)) {
            throw new ResourceNotFoundException(String.format("API key not found, id %s", keyId));
        }
        if (apiKey.getRevokedAt() == null) {
            apiKey.setRevokedAt(Instant.now());
        }
    }

    public Introspection introspect(String rawKey) {

        var keyHash = TokenUtil.sha256(rawKey);
        var opt = apikeyRepository.findIntrospectionByKeyHash(keyHash);
        var active = false;
        if (opt.isEmpty()) {
            return new Introspection(false, null, null, null, null);
        }
        var introspection = opt.get();
        var revokedAt = introspection.getRevokedAt();
        var expiresAt = introspection.getExpiresAt();
        var subscriptionStatus = introspection.getSubStatus();
        var apiStatus = introspection.getApiStatus();
        active = revokedAt == null && (expiresAt == null || expiresAt.isAfter(Instant.now()));
        if (active) {
            active =  apiStatus == ApiStatus.PUBLISHED && subscriptionStatus == SubscriptionStatus.ACTIVE;
        }
        if (!active) {
            return new Introspection(false, null, null, null, null);
        }
        var consumerId = introspection.getConsumerId();
        var apiId = introspection.getApiId();
        var plan = introspection.getPlan();
        var basePath = introspection.getBasePath();
        return new Introspection(active, consumerId, apiId, basePath, plan);
    }
}
