package com.media.portal.developerportal.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApiTests {

    @Test
    public void testCreateTransition() {
        var api = new Api();
        api.setName("v1/dummy");
        assertEquals(ApiStatus.DRAFT, api.getStatus());
    }

    @Test
    public void testPublishTransition() {
        var api = new Api();
        api.setName("v1/dummy");
        api.setOpenApiSpec("dummy");
        api.publish();
        assertEquals(ApiStatus.PUBLISHED, api.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenPublishedWithoutOpenSpec() {
        var api = new Api();
        api.setName("v1/dummy");
        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                api::publish
        );
        assertEquals("Cannot publish the API v1/dummy, missed OpenApi spec", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeprecatedAndNotPublished() {
        var api = new Api();
        api.setName("v1/dummy");
        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                api::deprecate
        );
        assertEquals("Illegal transition for API v1/dummy status DRAFT -> DEPRECATED", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPublishedWhenDeprecated() {
        var api = new Api();
        api.setName("v1/dummy");
        api.setOpenApiSpec("dummy");
        api.publish();
        api.deprecate();
        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                api::publish
        );
        assertEquals("Illegal transition for API v1/dummy status DEPRECATED -> PUBLISHED", exception.getMessage());
    }
}
