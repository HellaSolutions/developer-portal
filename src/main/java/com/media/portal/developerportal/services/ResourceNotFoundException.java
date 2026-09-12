package com.media.portal.developerportal.services;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String format) {
        super(format);
    }
}
