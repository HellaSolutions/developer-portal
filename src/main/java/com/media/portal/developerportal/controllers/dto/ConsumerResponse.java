package com.media.portal.developerportal.controllers.dto;

import com.media.portal.developerportal.model.Consumer;

public record ConsumerResponse(String name, String email, String organisation) {

    public static ConsumerResponse mapConsumer(Consumer consumer) {
        return new ConsumerResponse(consumer.getName(), consumer.getEmail(), consumer.getOrganisation());
    }
}
