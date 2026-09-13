package com.media.portal.developerportal.services;

import com.media.portal.developerportal.model.Consumer;
import com.media.portal.developerportal.repositories.ConsumerRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);

    private final ConsumerRepository consumerRepository;

    public ConsumerService(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    public Long createConsumer(Consumer consumer) {
        var savedConsumer = consumerRepository.save(consumer);
        return savedConsumer.getId();
    }

    public Consumer getConsumer(@Valid Long id) {
        return consumerRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(String.format("API with id %s not found", id)));
    }
}
