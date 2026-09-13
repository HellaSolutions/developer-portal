package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.controllers.dto.IntrospectRequest;
import com.media.portal.developerportal.services.Introspection;
import com.media.portal.developerportal.services.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/introspect")
public class IntrospectController {

    private final SubscriptionService subscriptionService;

    public IntrospectController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<Introspection> introspect(@RequestBody @Valid IntrospectRequest introspectRequest){
        var introspection = subscriptionService.introspect(introspectRequest.key());
        return ResponseEntity.ok(introspection);
    }
}
