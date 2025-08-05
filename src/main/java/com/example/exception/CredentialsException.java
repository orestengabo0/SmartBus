package com.example.exception;

public class CredentialsException extends RuntimeException {
    public CredentialsException(String message) {
        super(message);
    }
}
