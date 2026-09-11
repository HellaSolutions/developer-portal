package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
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
    private UUID uuid = UUID.randomUUID();

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

    public void publish(){
        var legal = this.status == ApiStatus.DRAFT;
        if (!legal) {
            throw new IllegalStateTransitionException(
                    String.format("Illegal transition for API %s status %s -> %s", this.name, this.status, ApiStatus.PUBLISHED));
        }
        if (this.openApiSpec == null || this.openApiSpec.isBlank()) {
            throw new IllegalStateTransitionException(
                    String.format("Cannot publish the API %s, missed OpenApi spec", this.name));
        }
        this.status = ApiStatus.PUBLISHED;
    }

    public void deprecate(){
        var legal = this.status == ApiStatus.PUBLISHED;
        if (!legal) {
            throw new IllegalStateTransitionException(
                    String.format("Illegal transition for API status %s -> %s", this.status, ApiStatus.DEPRECATED));
        }
        this.status = ApiStatus.DEPRECATED;
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

        if (this == o) return true;
        if (o == null) return false;
        Class<?> oClass = Hibernate.getClass(o);
        if (Hibernate.getClass(this) != oClass) return false;
        Api c = (Api) o;
        return Objects.equals(this.uuid, c.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
