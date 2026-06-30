package com.unomi.webhook.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.unomi.action.messaging.ActionExecutionCommand;
import com.unomi.pipeline.ProcessedMessageService;
import com.unomi.webhook.WebhookProcessingService;

@Service
@ConditionalOnProperty(prefix = "unomi.kafka.consumers.webhook", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebhookProcessingConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookProcessingConsumer.class);

    private final WebhookProcessingService webhookProcessingService;
    private final ProcessedMessageService processedMessageService;

    public WebhookProcessingConsumer(
        WebhookProcessingService webhookProcessingService,
        ProcessedMessageService processedMessageService
    ) {
        this.webhookProcessingService = webhookProcessingService;
        this.processedMessageService = processedMessageService;
    }

    @KafkaListener(
        topics = "${unomi.kafka.webhook.processing-topic:action-processing-webhook}",
        groupId = "${UNOMI_WEBHOOK_CONSUMER_GROUP:unomi-webhook-workers}"
    )
    public void consume(ActionExecutionCommand command) {
        LOGGER.info(
            "Processing webhook action event {} trackingId={} profileId={}",
            command.actionEventId(),
            command.trackingId() == null ? command.actionEventId() : command.trackingId(),
            command.profileId()
        );
        processedMessageService.processOnce(
            command.actionEventId().toString(),
            "WEBHOOK_PROCESSING",
            () -> webhookProcessingService.process(command)
        );
    }
}
