package com.media.portal.developerportal.repositories;

import com.media.portal.developerportal.model.ApiKey;
import com.media.portal.developerportal.model.Subscription;
import com.media.portal.developerportal.services.interfaces.IntrospectionView;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApyKeyRepository extends JpaRepository<ApiKey, Long> {

    List<ApiKey> findBySubscription(Subscription subscription);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT k FROM ApiKey k WHERE k.id = :id")
    Optional<ApiKey> findByIdForUpdate(@Param("id") Long id);


    @Query("""
            SELECT ak.expiresAt AS expiresAt, ak.revokedAt AS revokedAt,
                   api.id AS apiId, api.basePath AS basePath, api.status AS apiStatus,
                   sub.status AS subStatus, sub.consumer.id AS consumerId, sub.plan AS plan
            FROM ApiKey ak
            JOIN ak.subscription sub
            JOIN sub.api api
            WHERE ak.keyHash = :keyHash
            """)
    Optional<IntrospectionView> findIntrospectionByKeyHash(@Param("keyHash") String keyHash);
}
