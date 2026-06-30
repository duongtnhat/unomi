package com.unomi.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.action.messaging.ActionExecutionCommand;
import com.unomi.rule.RuleActionEventEntity;
import com.unomi.rule.RuleActionEventRepository;
import com.unomi.shared.NotFoundException;

@Service
public class ActionExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionExecutionService.class);

    private final RuleActionEventRepository repository;
    private final ActionTypeDefinitionService actionTypeDefinitionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ActionExecutionService(
        RuleActionEventRepository repository,
        ActionTypeDefinitionService actionTypeDefinitionService,
        KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.repository = repository;
        this.actionTypeDefinitionService = actionTypeDefinitionService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public RuleActionEventEntity resolve(ActionExecutionCommand command) {
        RuleActionEventEntity entity = repository.findById(command.actionEventId())
            .orElseThrow(() -> new NotFoundException("Rule action event not found: " + command.actionEventId()));
        ActionTypeDefinitionResponse actionType = actionTypeDefinitionService.activeByKey().get(command.actionType());
        if (actionType == null) {
            throw new NotFoundException("Active action type definition not found: " + command.actionType());
        }
        if (entity.getTrackingId() == null) {
            entity.setTrackingId(trackingId(command));
        }

        forward(actionType.processingChannel(), command);

        LOGGER.info(
            "Forwarded action event {} trackingId={} actionKey={} actionType={} profileId={} ruleKey={} processingChannel={}",
            command.actionEventId(),
            trackingId(command),
            command.actionKey(),
            command.actionType(),
            command.profileId(),
            command.ruleKey(),
            actionType.processingChannel()
        );
        entity.setStatus("RESOLVED");
        return entity;
    }

    private void forward(String topic, ActionExecutionCommand command) {
        try {
            kafkaTemplate.send(topic, command.profileId(), command).get();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to forward action event to topic: " + topic, exception);
        }
    }

    private java.util.UUID trackingId(ActionExecutionCommand command) {
        return command.trackingId() == null ? command.actionEventId() : command.trackingId();
    }
}
