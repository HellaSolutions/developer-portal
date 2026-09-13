package com.media.portal.developerportal.controllers.dto;

import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.ApiStatus;

import java.time.Instant;

public record ApiResponse(String name, String basePath, ApiStatus status, String ownerTeam, String openApiSpec,
                          Instant createdAt, Instant updatedAt) {
    public static ApiResponse mapApi(Api api) {
        return new ApiResponse(api.getName(), api.getBasePath(), api.getStatus(), api.getOwnerTeam(), api.getOpenApiSpec(), api.getCreatedAt(), api.getUpdatedAt());
    }
}
