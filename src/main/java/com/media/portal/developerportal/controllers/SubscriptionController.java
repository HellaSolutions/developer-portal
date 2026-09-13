package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.controllers.dto.ApiKeyCreateResponse;
import com.media.portal.developerportal.services.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // /subscriptions/{id}/keys
    @PostMapping("/{id}/keys")
    public ResponseEntity<ApiKeyCreateResponse> generateKey(@PathVariable Long id) {
        var key = subscriptionService.generateKey(id);
        return ResponseEntity.ok(key);
    }

    @DeleteMapping("/{id}/keys/{keyId}")
    public ResponseEntity<Void> revokeSubscription(@PathVariable Long subscriptionId, @PathVariable Long keyId) {
        subscriptionService.revoke(subscriptionId, keyId);
        return ResponseEntity.ok().build();
    }
}
