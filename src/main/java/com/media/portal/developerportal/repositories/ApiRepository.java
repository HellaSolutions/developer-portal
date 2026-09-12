package com.media.portal.developerportal.repositories;

import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.ApiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ApiRepository extends JpaRepository<Api, Long> {

    @Query("SELECT a FROM Api a WHERE a.status = :status")
    Page<Api> findAllByStatus(@Param("status") ApiStatus status, Pageable pageable);
}
