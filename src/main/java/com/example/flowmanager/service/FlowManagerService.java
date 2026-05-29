package com.example.flowmanager.service;

import com.example.flowmanager.dto.ConversionRequestEvent;
import com.example.flowmanager.dto.ConversionResultEvent;
import com.example.flowmanager.dto.ConversionStatusResponse;
import com.example.flowmanager.dto.UploadResponse;
import com.example.flowmanager.entity.ConversionStatus;
import com.example.flowmanager.entity.ConversionTask;
import com.example.flowmanager.entity.OutboxEvent;
import com.example.flowmanager.entity.OutboxEventType;
import com.example.flowmanager.exception.FileReadException;
import com.example.flowmanager.exception.OutboxSerializationException;
import com.example.flowmanager.exception.TaskNotFoundException;
import com.example.flowmanager.factory.ConversionTaskFactory;
import com.example.flowmanager.repository.ConversionTaskRepository;
import com.example.flowmanager.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowManagerService {

    private final ConversionTaskRepository conversionTaskRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final MinioStorageService minioStorageService;
    private final ConversionTaskFactory conversionTaskFactory;
    private final ObjectMapper objectMapper;

    @Transactional
    public UploadResponse upload(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String objectKey = UUID.randomUUID() + "_" + originalFileName;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new FileReadException("Failed to read uploaded file", e);
        }

        String bucket = minioStorageService.uploadFile(objectKey, bytes, file.getContentType());

        ConversionTask task = conversionTaskFactory.createPending(originalFileName, bucket, objectKey);
        conversionTaskRepository.save(task);

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateId(task.getId());
        outboxEvent.setEventType(OutboxEventType.CONVERSION_REQUEST);
        outboxEvent.setCreatedAt(LocalDateTime.now());
        try {
            outboxEvent.setPayload(objectMapper.writeValueAsString(
                    new ConversionRequestEvent(task.getId().toString(), bucket, objectKey)
            ));
        } catch (JsonProcessingException e) {
            throw new OutboxSerializationException("Failed to serialize outbox event payload", e);
        }
        outboxEventRepository.save(outboxEvent);

        log.info("Task created: id={}, key={}", task.getId(), objectKey);

        return new UploadResponse(task.getId(), task.getStatus());
    }

    public ConversionStatusResponse getStatus(UUID id) {
        ConversionTask task = conversionTaskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return toStatusResponse(task);
    }

    public byte[] downloadResult(UUID id) {
        ConversionTask task = conversionTaskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getStatus() != ConversionStatus.SUCCESS) {
            throw new IllegalStateException("File is not ready for download: status=" + task.getStatus());
        }

        return minioStorageService.downloadFile(task.getResultBucket(), task.getResultKey());
    }

    @Transactional
    public void handleConversionResult(ConversionResultEvent event) {
        UUID id;
        try {
            id = UUID.fromString(event.eventId());
        } catch (IllegalArgumentException e) {
            log.error("Received result event with invalid UUID eventId: {}", event.eventId());
            return;
        }

        ConversionTask task = conversionTaskRepository.findById(id).orElse(null);
        if (task == null) {
            log.error("Task not found for eventId: {}", event.eventId());
            return;
        }

        if (event.errorMessage() != null) {
            task.setStatus(ConversionStatus.ERROR);
            task.setErrorMessage(event.errorMessage());
            log.info("Task marked as ERROR: id={}, error={}", id, event.errorMessage());
        } else {
            task.setStatus(ConversionStatus.SUCCESS);
            task.setResultBucket(event.resultBucket());
            task.setResultKey(event.resultKey());
            log.info("Task marked as SUCCESS: id={}, resultKey={}", id, event.resultKey());
        }

        task.setUpdatedAt(LocalDateTime.now());
        conversionTaskRepository.save(task);
    }

    private ConversionStatusResponse toStatusResponse(ConversionTask task) {
        return new ConversionStatusResponse(
                task.getId(),
                task.getOriginalFileName(),
                task.getStatus(),
                task.getErrorMessage(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
