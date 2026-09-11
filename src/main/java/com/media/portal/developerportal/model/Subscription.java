package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
//Subscription — a consumer subscribed to a published API: id, consumer, api, plan (FREE | STANDARD | PARTNER), status (ACTIVE | SUSPENDED | REVOKED), createdAt
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "consumer_id", nullable = false)
    private Consumer consumer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @CreatedDate
    LocalDateTime createdAt;
}
