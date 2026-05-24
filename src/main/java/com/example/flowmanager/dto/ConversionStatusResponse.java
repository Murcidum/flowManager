package com.example.flowmanager.dto;

import com.example.flowmanager.entity.ConversionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversionStatusResponse(
        UUID id,
        String originalFileName,
        ConversionStatus status,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
