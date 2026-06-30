package com.unomi.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public ActionExecutionService(RuleActionEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RuleActionEventEntity resolve(ActionExecutionCommand command) {
        RuleActionEventEntity entity = repository.findById(command.actionEventId())
            .orElseThrow(() -> new NotFoundException("Rule action event not found: " + command.actionEventId()));

        LOGGER.info(
            "Resolved action event {} actionKey={} actionType={} profileId={} ruleKey={}",
            command.actionEventId(),
            command.actionKey(),
            command.actionType(),
            command.profileId(),
            command.ruleKey()
        );
        entity.setStatus("RESOLVED");
        return entity;
    }
}
