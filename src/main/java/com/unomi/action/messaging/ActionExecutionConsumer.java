package com.unomi.action.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.unomi.action.ActionExecutionService;
import com.unomi.pipeline.ProcessedMessageService;

@Service
@ConditionalOnProperty(prefix = "unomi.kafka.consumers.action", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ActionExecutionConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionExecutionConsumer.class);

    private final ActionExecutionService actionExecutionService;
    private final ProcessedMessageService processedMessageService;

    public ActionExecutionConsumer(
        ActionExecutionService actionExecutionService,
        ProcessedMessageService processedMessageService
    ) {
        this.actionExecutionService = actionExecutionService;
        this.processedMessageService = processedMessageService;
    }

    @KafkaListener(
        topics = "${unomi.kafka.topics.action-execution}",
        groupId = "${UNOMI_ACTION_CONSUMER_GROUP:unomi-action-workers}"
    )
    public void consume(ActionExecutionCommand command) {
        LOGGER.info("Handling action event command {}", command.actionEventId());
        processedMessageService.processOnce(
            command.actionEventId().toString(),
            "ACTION_EXECUTION",
            () -> actionExecutionService.resolve(command)
        );
    }
}
