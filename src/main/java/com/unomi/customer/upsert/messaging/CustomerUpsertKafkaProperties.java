package com.unomi.customer.upsert.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "unomi.kafka.topics")
public record CustomerUpsertKafkaProperties(
    String customerUpsert,
    String profileMerge,
    String segmentQualification
) {
}
