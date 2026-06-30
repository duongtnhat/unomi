package com.unomi.email.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.unomi.action.messaging.ActionExecutionCommand;
import com.unomi.email.EmailProcessingService;
import com.unomi.pipeline.ProcessedMessageService;

@Service
@ConditionalOnProperty(prefix = "unomi.kafka.consumers.email", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailProcessingConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailProcessingConsumer.class);

    private final EmailProcessingService emailProcessingService;
    private final ProcessedMessageService processedMessageService;

    public EmailProcessingConsumer(
        EmailProcessingService emailProcessingService,
        ProcessedMessageService processedMessageService
    ) {
        this.emailProcessingService = emailProcessingService;
        this.processedMessageService = processedMessageService;
    }

    @KafkaListener(
        topics = "${unomi.kafka.email.processing-topic:action-processing-email}",
        groupId = "${UNOMI_EMAIL_CONSUMER_GROUP:unomi-email-workers}"
    )
    public void consume(ActionExecutionCommand command) {
        LOGGER.info(
            "Processing email action event {} trackingId={} profileId={}",
            command.actionEventId(),
            command.trackingId() == null ? command.actionEventId() : command.trackingId(),
            command.profileId()
        );
        processedMessageService.processOnce(
            command.actionEventId().toString(),
            "EMAIL_PROCESSING",
            () -> emailProcessingService.process(command)
        );
    }
}
