package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.controllers.dto.ConsumerCreateRequest;
import com.media.portal.developerportal.controllers.dto.ConsumerResponse;
import com.media.portal.developerportal.controllers.dto.SubscriptionCreateRequest;
import com.media.portal.developerportal.model.Consumer;
import com.media.portal.developerportal.services.ConsumerService;
import com.media.portal.developerportal.services.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class ConsumerController {

    private final ConsumerService consumerService;
    private final SubscriptionService subscriptionService;

    public ConsumerController(ConsumerService consumerService, SubscriptionService subscriptionService) {
        this.consumerService = consumerService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<Long> createApi(@Valid @RequestBody ConsumerCreateRequest request){
        var consumer = new Consumer();
        consumer.setName(request.name());
        consumer.setEmail(request.email());
        consumer.setOrganisation(request.organisation());

        var id = consumerService.createConsumer(consumer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", String.format("/v1/consumers/%s", id)).body(id);
    }

    @PostMapping("/{id}/subscriptions")
    public ResponseEntity<Long> subscribeApi(@PathVariable Long consumerId, @Valid @RequestBody SubscriptionCreateRequest request) {
        var id = subscriptionService.createSubscription(consumerId, request.apiId(), request.plan());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", id.toString())
                .body(id);
    }



        @GetMapping("/{id}")
    public ResponseEntity<ConsumerResponse> getConsumer(@Valid @PathVariable Long id){
        var consumer = consumerService.getConsumer(id);
        return ResponseEntity.status(HttpStatus.OK).body(ConsumerResponse.mapConsumer(consumer));
    }
}
