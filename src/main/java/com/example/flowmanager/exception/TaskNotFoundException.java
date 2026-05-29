package com.example.flowmanager.exception;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(UUID id) {
        super("Conversion task not found: " + id);
    }
}
