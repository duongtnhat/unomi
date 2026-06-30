package com.unomi.action.messaging;

import org.springframework.stereotype.Service;

import com.unomi.customer.upsert.messaging.CustomerUpsertKafkaProperties;
import com.unomi.pipeline.OutboxService;

@Service
public class ActionExecutionCommandPublisher {

    private final CustomerUpsertKafkaProperties properties;
    private final OutboxService outboxService;

    public ActionExecutionCommandPublisher(
        CustomerUpsertKafkaProperties properties,
        OutboxService outboxService
    ) {
        this.properties = properties;
        this.outboxService = outboxService;
    }

    public void publish(ActionExecutionCommand command) {
        String actionEventId = command.actionEventId().toString();
        outboxService.enqueue(actionEventId, properties.actionExecution(), command.profileId(), command);
    }
}
