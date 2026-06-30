package com.unomi.customer.upsert.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.unomi.customer.upsert.UpsertCustomerInfoProcessor;
import com.unomi.pipeline.ProcessedMessageService;

@Service
@ConditionalOnProperty(prefix = "unomi.kafka.consumers.write-es", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CustomerUpsertWriteElasticsearchConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerUpsertWriteElasticsearchConsumer.class);

    private final UpsertCustomerInfoProcessor processor;
    private final UpsertCustomerCommandPublisher publisher;
    private final ProcessedMessageService processedMessageService;

    public CustomerUpsertWriteElasticsearchConsumer(
        UpsertCustomerInfoProcessor processor,
        UpsertCustomerCommandPublisher publisher,
        ProcessedMessageService processedMessageService
    ) {
        this.processor = processor;
        this.publisher = publisher;
        this.processedMessageService = processedMessageService;
    }

    @KafkaListener(
        topics = "${unomi.kafka.topics.customer-upsert}",
        groupId = "${UNOMI_WRITE_ES_CONSUMER_GROUP:unomi-write-es-workers}"
    )
    public void consume(UpsertCustomerCommand command) {
        LOGGER.info("Writing Elasticsearch for customer upsert command {}", command.messageId());
        processedMessageService.processOnce(
            command.messageId(),
            "WRITE_ES",
            () -> {
                ElasticsearchWriteCompletedCommand completedCommand = processor.writeElasticsearch(command);
                if (!command.skipHook()) {
                    publisher.publish(completedCommand);
                }
                return completedCommand;
            }
        );
    }
}
