package com.example.flowmanager.dto;

public record ConversionResultEvent(
        String eventId,
        String sourceBucket,
        String sourceKey,
        String resultBucket,
        String resultKey,
        String errorMessage
) {}
