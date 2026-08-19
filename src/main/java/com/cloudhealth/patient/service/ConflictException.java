package com.cloudhealth.patient.service;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

