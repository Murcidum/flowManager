package com.example.flowmanager.dto;

import com.example.flowmanager.entity.ConversionStatus;

import java.util.UUID;

public record UploadResponse(
        UUID id,
        ConversionStatus status
) {}
