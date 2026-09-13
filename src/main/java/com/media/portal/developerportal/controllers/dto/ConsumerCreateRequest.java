package com.media.portal.developerportal.controllers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConsumerCreateRequest(

        @NotBlank(message = "name is required")
        @Size(min = 3, max = 60, message = "name must be between 3 and 60 characters")
        String name,

        @NotBlank(message = "email is required")
        @Email(message = "invalid email")
        String email,

        @NotBlank(message = "organisation is required")
        @Pattern(
                regexp = "^/[a-z0-9-]+/v[0-9]+$",
                message = "basePath must be a name followed by a version, e.g. /scopus/v1"
        )
        String organisation
) {
}
