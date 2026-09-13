package com.media.portal.developerportal.controllers.dto;

import com.media.portal.developerportal.model.SubscriptionType;

public record SubscriptionCreateRequest(Long apiId, SubscriptionType plan) {
}
