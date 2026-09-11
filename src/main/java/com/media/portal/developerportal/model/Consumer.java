package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
//Consumer — id, name, email, organisation, createdAt.
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consumer_seq")
    @SequenceGenerator(
            name = "consumer_seq",
            sequenceName = "consumer_seq",
            allocationSize = 50
    )
    private  Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    private  String name;

    @Column(unique = true, nullable = false)
    private  String email;

    @Column(nullable = false)
    private  String organisation;

    @CreatedDate
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public Object getUuid() {
        return this.uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null) return false;
        Class<?> oClass = Hibernate.getClass(o);
        if (Hibernate.getClass(this) != oClass) return false;
        Consumer api = (Consumer) o;
        return Objects.equals(this.uuid, api.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
