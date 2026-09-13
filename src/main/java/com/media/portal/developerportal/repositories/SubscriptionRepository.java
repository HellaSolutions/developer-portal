package com.media.portal.developerportal.repositories;

import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.Consumer;
import com.media.portal.developerportal.model.Subscription;
import com.media.portal.developerportal.model.SubscriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByConsumerAndApi(Consumer consumer, Api api);

    boolean existsByConsumerAndApiAndStatusNot(Consumer consumer, Api api, SubscriptionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s WHERE s.id = :id")
    Optional<Subscription> findByIdForUpdate(@Param("id") Long id);
}
