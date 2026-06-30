package com.unomi.customer.upsert.messaging;

import org.springframework.stereotype.Service;

import com.unomi.pipeline.OutboxService;

@Service
public class UpsertCustomerCommandPublisher {

    private final CustomerUpsertKafkaProperties properties;
    private final OutboxService outboxService;

    public UpsertCustomerCommandPublisher(
        CustomerUpsertKafkaProperties properties,
        OutboxService outboxService
    ) {
        this.properties = properties;
        this.outboxService = outboxService;
    }

    public void publish(UpsertCustomerCommand command) {
        outboxService.enqueue(command.messageId(), properties.customerUpsert(), command.messageId(), command);
    }

    public void publish(ElasticsearchWriteCompletedCommand command) {
        outboxService.enqueue(command.messageId(), properties.profileMerge(), command.messageId(), command);
    }

    public void publish(ProfileMergeCompletedCommand command) {
        outboxService.enqueue(command.messageId(), properties.segmentQualification(), command.messageId(), command);
    }

    public void publish(RuleEvaluationCommand command) {
        outboxService.enqueue(command.messageId(), properties.ruleEvaluation(), command.messageId(), command);
    }
}
