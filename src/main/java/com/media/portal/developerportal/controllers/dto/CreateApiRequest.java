package com.media.portal.developerportal.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateApiRequest(

        @NotBlank(message = "name is required")
        @Size(min = 3, max = 60, message = "name must be between 3 and 60 characters")
        String name,

        @NotBlank(message = "basePath is required")
        @Pattern(
                regexp = "^/[a-z0-9-]+/v[0-9]+$",
                message = "basePath must be a name followed by a version, e.g. /scopus/v1"
        )
        String basePath,

        @NotBlank(message = "ownerTeam is required")
        String ownerTeam,

        String openApiSpec

) {
}
