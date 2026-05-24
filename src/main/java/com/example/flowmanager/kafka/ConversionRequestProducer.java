package com.example.flowmanager.kafka;

import com.example.flowmanager.dto.ConversionRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversionRequestProducer {

    private final KafkaTemplate<String, ConversionRequestEvent> kafkaTemplate;

    @Value("${kafka.topics.conversion-request}")
    private String requestTopic;

    public void sendRequest(ConversionRequestEvent event) {
        log.info("Sending conversion request: eventId={}, bucket={}, key={}",
                event.eventId(), event.bucket(), event.objectKey());
        try {
            kafkaTemplate.send(requestTopic, event.eventId(), event).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send conversion request to Kafka: " + event.eventId(), e);
        }
    }
}
