package com.example.exception;

public class QRCodeException extends RuntimeException {
    public QRCodeException(String message) {
        super(message);
    }
}
