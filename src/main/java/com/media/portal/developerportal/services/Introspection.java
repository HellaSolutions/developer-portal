package com.media.portal.developerportal.services;

import com.media.portal.developerportal.model.SubscriptionType;

public record Introspection(boolean active, Long consumerId, Long apiId, String basePath, SubscriptionType plan)
{ }

