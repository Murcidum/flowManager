package com.example.flowmanager.exception;

public class MinioStorageException extends RuntimeException {

    public MinioStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
