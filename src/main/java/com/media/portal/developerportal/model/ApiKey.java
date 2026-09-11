package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
//ApiKey — belongs to a subscription: id, keyHash, prefix (first 8 chars, for display), createdAt, expiresAt, revokedAt. The raw key is returned once at creation and never stored.
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyHash;

    @Column(nullable = false)
    private String prefix;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @CreatedDate
    private Instant createdAt;

    private Instant expiresAt;

    @Column
    private Instant revokedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApiKey apiKey = (ApiKey) o;
        return Objects.equals(id, apiKey.id) && Objects.equals(keyHash, apiKey.keyHash) && Objects.equals(prefix, apiKey.prefix) && Objects.equals(subscription, apiKey.subscription) && Objects.equals(createdAt, apiKey.createdAt) && Objects.equals(expiresAt, apiKey.expiresAt) && Objects.equals(revokedAt, apiKey.revokedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keyHash, prefix, subscription, createdAt, expiresAt, revokedAt);
    }
}
