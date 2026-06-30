package com.unomi.customer.upsert.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UpsertCustomerCommandPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CustomerUpsertKafkaProperties properties;

    public UpsertCustomerCommandPublisher(
        KafkaTemplate<String, Object> kafkaTemplate,
        CustomerUpsertKafkaProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public void publish(UpsertCustomerCommand command) {
        kafkaTemplate.send(properties.customerUpsert(), command.messageId(), command);
    }

    public void publish(ElasticsearchWriteCompletedCommand command) {
        kafkaTemplate.send(properties.profileMerge(), command.messageId(), command);
    }

    public void publish(ProfileMergeCompletedCommand command) {
        kafkaTemplate.send(properties.segmentQualification(), command.messageId(), command);
    }
}
