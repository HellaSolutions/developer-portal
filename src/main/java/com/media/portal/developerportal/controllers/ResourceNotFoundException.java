package com.media.portal.developerportal.controllers;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String format) {
        super(format);
    }
}
