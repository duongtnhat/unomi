package com.unomi.customer.upsert.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(CustomerUpsertKafkaProperties.class)
public class CustomerUpsertKafkaConfig {

    @Bean
    NewTopic customerUpsertTopic(CustomerUpsertKafkaProperties properties) {
        return TopicBuilder.name(properties.customerUpsert())
            .partitions(6)
            .replicas(1)
            .build();
    }

    @Bean
    NewTopic profileMergeTopic(CustomerUpsertKafkaProperties properties) {
        return TopicBuilder.name(properties.profileMerge())
            .partitions(6)
            .replicas(1)
            .build();
    }

    @Bean
    NewTopic segmentQualificationTopic(CustomerUpsertKafkaProperties properties) {
        return TopicBuilder.name(properties.segmentQualification())
            .partitions(6)
            .replicas(1)
            .build();
    }
}
