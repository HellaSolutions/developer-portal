package com.media.portal.developerportal.repositories;

import com.media.portal.developerportal.model.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, Long> {
}
