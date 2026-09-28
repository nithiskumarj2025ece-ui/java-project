package com.example.garagedesk.exception;

// Thrown when the request conflicts with the current data, e.g. bay occupied (returns HTTP 409)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
