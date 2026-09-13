package com.media.portal.developerportal.services.interfaces;

import com.media.portal.developerportal.model.ApiStatus;
import com.media.portal.developerportal.model.SubscriptionStatus;
import com.media.portal.developerportal.model.SubscriptionType;

import java.time.Instant;

public interface IntrospectionView {
    Instant getExpiresAt();
    Instant getRevokedAt();
    Long getApiId();
    String getBasePath();
    ApiStatus getApiStatus();
    SubscriptionStatus getSubStatus();
    Long getConsumerId();
    SubscriptionType getPlan();
}
