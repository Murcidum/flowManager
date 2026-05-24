package com.example.flowmanager.dto;

public record ConversionRequestEvent(
        String eventId,
        String bucket,
        String objectKey
) {}
