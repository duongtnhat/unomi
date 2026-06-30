package com.unomi.pipeline;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.unomi.action.messaging.ActionExecutionCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unomi.customer.upsert.messaging.ElasticsearchWriteCompletedCommand;
import com.unomi.customer.upsert.messaging.ProfileMergeCompletedCommand;
import com.unomi.customer.upsert.messaging.RuleEvaluationCommand;
import com.unomi.customer.upsert.messaging.UpsertCustomerCommand;

@Service
@ConditionalOnProperty(prefix = "unomi.kafka.outbox.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxKafkaPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxKafkaPublisher.class);

    private final OutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxKafkaPublisher(
        OutboxService outboxService,
        KafkaTemplate<String, Object> kafkaTemplate,
        ObjectMapper objectMapper
    ) {
        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${unomi.kafka.outbox.publisher.fixed-delay-ms:1000}")
    public void publishPending() {
        for (OutboxEventEntity event : outboxService.pendingBatch()) {
            try {
                Object payload = deserialize(event.getPayloadType(), event.getPayload());
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), payload).get();
                outboxService.markPublished(event);
            } catch (Exception exception) {
                LOGGER.warn("Unable to publish outbox event {}", event.getId(), exception);
                outboxService.markFailed(event, exception);
            }
        }
    }

    private Object deserialize(String payloadType, Map<String, Object> payload) {
        return switch (payloadType) {
            case "com.unomi.customer.upsert.messaging.UpsertCustomerCommand" ->
                objectMapper.convertValue(payload, UpsertCustomerCommand.class);
            case "com.unomi.customer.upsert.messaging.ElasticsearchWriteCompletedCommand" ->
                objectMapper.convertValue(payload, ElasticsearchWriteCompletedCommand.class);
            case "com.unomi.customer.upsert.messaging.ProfileMergeCompletedCommand" ->
                objectMapper.convertValue(payload, ProfileMergeCompletedCommand.class);
            case "com.unomi.customer.upsert.messaging.RuleEvaluationCommand" ->
                objectMapper.convertValue(payload, RuleEvaluationCommand.class);
            case "com.unomi.action.messaging.ActionExecutionCommand" ->
                objectMapper.convertValue(payload, ActionExecutionCommand.class);
            default -> throw new IllegalArgumentException("Unsupported outbox payload type: " + payloadType);
        };
    }
}
