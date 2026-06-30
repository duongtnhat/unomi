package com.unomi.customer.upsert.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.unomi.customer.upsert.UpsertCustomerInfoProcessor;

@Service
@ConditionalOnProperty(prefix = "unomi.kafka.consumers.merge", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ProfileMergeConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileMergeConsumer.class);

    private final UpsertCustomerInfoProcessor processor;
    private final UpsertCustomerCommandPublisher publisher;

    public ProfileMergeConsumer(
        UpsertCustomerInfoProcessor processor,
        UpsertCustomerCommandPublisher publisher
    ) {
        this.processor = processor;
        this.publisher = publisher;
    }

    @KafkaListener(
        topics = "${unomi.kafka.topics.profile-merge}",
        groupId = "${UNOMI_MERGE_CONSUMER_GROUP:unomi-merge-workers}"
    )
    public void consume(ElasticsearchWriteCompletedCommand command) {
        LOGGER.info("Merging profile for customer upsert command {}", command.messageId());
        publisher.publish(processor.mergeProfile(command));
    }
}
