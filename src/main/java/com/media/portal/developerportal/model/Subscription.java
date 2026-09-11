package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
//Subscription — a consumer subscribed to a published API: id, consumer, api, plan (FREE | STANDARD | PARTNER), status (ACTIVE | SUSPENDED | REVOKED), createdAt
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "consumer_id", nullable = false)
    private Consumer consumer;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "api_id", nullable = false)
    private Consumer api;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType plan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @CreatedDate
    Instant createdAt;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id) && Objects.equals(consumer, that.consumer) && Objects.equals(api, that.api) && plan == that.plan && status == that.status && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, consumer, api, plan, status, createdAt);
    }
}
