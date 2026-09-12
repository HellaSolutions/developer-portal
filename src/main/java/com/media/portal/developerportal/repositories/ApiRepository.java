package com.media.portal.developerportal.repositories;

import com.media.portal.developerportal.model.Api;
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

    @Query(value = "SELECT api FROM Api api WHERE api.name = :name LIMIT 1", nativeQuery = true)
    List<Api> nameExists(@Param("name") String name);

    Page<Api> findAll(String status, Pageable pageable);
}
