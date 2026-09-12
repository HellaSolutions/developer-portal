package com.media.portal.developerportal.services;

public class BadApiRequestException extends RuntimeException {
    public BadApiRequestException(String format) {
        super(format);
    }
}
