package com.example.flowmanager.exception;

public class OutboxSerializationException extends RuntimeException {

    public OutboxSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
