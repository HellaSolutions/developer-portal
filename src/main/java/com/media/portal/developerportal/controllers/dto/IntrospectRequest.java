package com.media.portal.developerportal.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public record IntrospectRequest(
        @NotBlank(message = "Key is required")
        String key
) {
}
