package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
//Api — id, name (unique), basePath (unique, e.g. /scopus/v1), status (DRAFT | PUBLISHED | DEPRECATED), ownerTeam, openApiSpec (text), createdAt, updatedAt.
public class Api {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_seq")
    @SequenceGenerator(
            name = "api_seq",
            sequenceName = "api_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private final UUID uuid = UUID.randomUUID();

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String basePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiStatus status = ApiStatus.DRAFT;

    @Column(nullable = false)
    private String ownerTeam;

    @Column(columnDefinition = "TEXT")
    private String openApiSpec;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public ApiStatus getStatus() {
        return status;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setStatus(ApiStatus status) throws IllegalStateTransitionException {
        var illegal =
                (this.status != ApiStatus.DRAFT && status == ApiStatus.DRAFT) ||
                (this.status == ApiStatus.PUBLISHED && status != ApiStatus.DEPRECATED) ||
                (this.status == ApiStatus.DRAFT && status != ApiStatus.PUBLISHED);
        if (illegal) {
            throw new IllegalStateTransitionException(
                    String.format("Illegal transition for API status %s -> %s", this.status, status));
        }
        this.status = status;
    }

    public String getOwnerTeam() {
        return ownerTeam;
    }

    public void setOwnerTeam(String ownerTeam) {
        this.ownerTeam = ownerTeam;
    }

    public String getOpenApiSpec() {
        return openApiSpec;
    }

    public void setOpenApiSpec(String openApiSpec) {
        this.openApiSpec = openApiSpec;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Api api = (Api) o;
        return Objects.equals(uuid, api.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
