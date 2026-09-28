package com.example.garagedesk.exception;

// Thrown when a request breaks a business rule (returns HTTP 400)
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
