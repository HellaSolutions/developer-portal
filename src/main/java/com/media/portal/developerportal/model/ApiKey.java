package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
//ApiKey — belongs to a subscription: id, keyHash, prefix (first 8 chars, for display), createdAt, expiresAt, revokedAt. The raw key is returned once at creation and never stored.
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "apikey_seq")
    @SequenceGenerator(
            name = "apikey_seq",
            sequenceName = "apikey_seq",
            allocationSize = 50
    )
    private Long id;


    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

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

    private Instant revokedAt;

    public Long getId() {
        return id;
    }

    public Object getUuid() {
        return uuid;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null) return false;
        Class<?> oClass = Hibernate.getClass(o);
        if (Hibernate.getClass(this) != oClass) return false;
        ApiKey api = (ApiKey) o;
        return Objects.equals(this.uuid, api.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
