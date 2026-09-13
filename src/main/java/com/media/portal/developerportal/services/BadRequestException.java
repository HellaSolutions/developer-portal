package com.media.portal.developerportal.services;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message){
        super(message);
    }
}
