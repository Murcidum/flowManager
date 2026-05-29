package com.example.flowmanager.kafka;

import com.example.flowmanager.dto.ConversionResultEvent;
import com.example.flowmanager.service.FlowManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversionResultConsumer {

    private final FlowManagerService flowManagerService;

    @KafkaListener(topics = "${kafka.topics.conversion-result}")
    public void consume(ConversionResultEvent event, Acknowledgment acknowledgment) {
        log.info("Received conversion result: eventId={}, resultKey={}, error={}",
                event.eventId(), event.resultKey(), event.errorMessage());
        try {
            flowManagerService.handleConversionResult(event);
        } catch (Exception e) {
            log.error("Failed to handle conversion result: eventId={}", event.eventId(), e);
        }
        acknowledgment.acknowledge();
    }
}
