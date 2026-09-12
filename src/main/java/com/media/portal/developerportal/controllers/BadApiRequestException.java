package com.media.portal.developerportal.controllers;

public class BadApiRequestException extends RuntimeException {
    public BadApiRequestException(String format) {
        super(format);
    }
}
