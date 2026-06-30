package com.unomi.customer.upsert.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.unomi.customer.upsert.UpsertCustomerInfoProcessor;
import com.unomi.pipeline.ProcessedMessageService;

@Service
@ConditionalOnProperty(prefix = "unomi.kafka.consumers.rule", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RuleEvaluationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleEvaluationConsumer.class);

    private final UpsertCustomerInfoProcessor processor;
    private final ProcessedMessageService processedMessageService;

    public RuleEvaluationConsumer(
        UpsertCustomerInfoProcessor processor,
        ProcessedMessageService processedMessageService
    ) {
        this.processor = processor;
        this.processedMessageService = processedMessageService;
    }

    @KafkaListener(
        topics = "${unomi.kafka.topics.rule-evaluation}",
        groupId = "${UNOMI_RULE_CONSUMER_GROUP:unomi-rule-workers}"
    )
    public void consume(RuleEvaluationCommand command) {
        LOGGER.info("Evaluating rules for customer upsert command {}", command.messageId());
        processedMessageService.processOnce(
            command.messageId(),
            "RULE_EVALUATION",
            () -> processor.evaluateRules(command)
        );
    }
}
