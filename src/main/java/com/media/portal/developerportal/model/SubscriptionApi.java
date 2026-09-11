package com.media.portal.developerportal.model;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

public class SubscriptionApi {

    @ManyToOne
    @JoinColumn(name = "api_id", nullable = false)
    private Api api;

    //Among all APIs associated to subscription only one should be ACTIVE or SUSPENDED.
    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @CreatedDate
    private LocalDateTime createdAt;

}
