package com.unomi.customer.upsert.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.unomi.customer.upsert.UpsertCustomerInfoProcessor;

@Service
@ConditionalOnProperty(prefix = "unomi.kafka.consumers.segment", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SegmentQualificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentQualificationConsumer.class);

    private final UpsertCustomerInfoProcessor processor;

    public SegmentQualificationConsumer(UpsertCustomerInfoProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(
        topics = "${unomi.kafka.topics.segment-qualification}",
        groupId = "${UNOMI_SEGMENT_CONSUMER_GROUP:unomi-segment-workers}"
    )
    public void consume(ProfileMergeCompletedCommand command) {
        LOGGER.info("Qualifying segments for customer upsert command {}", command.messageId());
        processor.qualifySegments(command);
    }
}
