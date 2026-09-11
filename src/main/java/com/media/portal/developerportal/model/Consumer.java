package com.media.portal.developerportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
//Consumer — id, name, email, organisation, createdAt.
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(nullable = false)
    private  String name;

    @Email
    @Column(unique = true, nullable = false)
    private  String email;

    @Column(nullable = false)
    private  String organisation;

    @CreatedDate
    private Instant createdAt;

    public Long getId() {
        return id;
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

    public Instant getCreateAt() {
        return createdAt;
    }

    public void setCreateAt(Instant createAt) {
        this.createdAt = createAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Consumer consumer = (Consumer) o;
        return Objects.equals(id, consumer.id) && Objects.equals(name, consumer.name) && Objects.equals(email, consumer.email) && Objects.equals(organisation, consumer.organisation) && Objects.equals(createdAt, consumer.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, organisation, createdAt);
    }
}
