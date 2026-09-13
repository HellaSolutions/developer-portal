package com.media.portal.developerportal.controllers.dto;


import com.media.portal.developerportal.model.SubscriptionStatus;
import com.media.portal.developerportal.model.SubscriptionType;

public record IntrospectResponse(boolean active, SubscriptionStatus subscriptionStatus, Long consumerId, Long apiId, String basePath, SubscriptionType subscriptionType) {}
