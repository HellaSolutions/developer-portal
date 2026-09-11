package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime revokedAt;

}
