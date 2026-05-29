package com.example.flowmanager.service;

import com.example.flowmanager.dto.ConversionRequestEvent;
import com.example.flowmanager.entity.ConversionStatus;
import com.example.flowmanager.entity.OutboxEvent;
import com.example.flowmanager.kafka.ConversionRequestProducer;
import com.example.flowmanager.repository.ConversionTaskRepository;
import com.example.flowmanager.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRelayProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final ConversionTaskRepository conversionTaskRepository;
    private final ConversionRequestProducer conversionRequestProducer;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(UUID outboxEventId) {
        OutboxEvent outboxEvent = outboxEventRepository.findById(outboxEventId).orElse(null);
        if (outboxEvent == null || outboxEvent.getProcessedAt() != null) {
            return;
        }

        ConversionRequestEvent kafkaEvent;
        try {
            kafkaEvent = objectMapper.readValue(outboxEvent.getPayload(), ConversionRequestEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize outbox event {}: {}", outboxEventId, e.getMessage());
            return;
        }

        conversionRequestProducer.sendRequest(kafkaEvent);

        outboxEvent.setProcessedAt(LocalDateTime.now());
        outboxEventRepository.save(outboxEvent);

        conversionTaskRepository.findById(outboxEvent.getAggregateId()).ifPresent(task -> {
            task.setStatus(ConversionStatus.PROCESSING);
            task.setUpdatedAt(LocalDateTime.now());
            conversionTaskRepository.save(task);
        });

        log.info("Outbox event processed: id={}, taskId={}", outboxEventId, outboxEvent.getAggregateId());
    }
}
