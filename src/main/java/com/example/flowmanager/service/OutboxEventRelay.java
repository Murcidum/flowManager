package com.example.flowmanager.service;

import com.example.flowmanager.entity.OutboxEvent;
import com.example.flowmanager.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxRelayProcessor processor;

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:5000}")
    public void relay() {
        List<OutboxEvent> pending = outboxEventRepository.findPendingEvents(Pageable.ofSize(50));
        if (pending.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox event(s)", pending.size());
        for (OutboxEvent event : pending) {
            try {
                processor.process(event.getId());
            } catch (Exception e) {
                log.error("Outbox relay failed for event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
