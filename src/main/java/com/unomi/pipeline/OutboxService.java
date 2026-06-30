package com.unomi.pipeline;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OutboxService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OutboxEventEntity enqueue(String messageId, String topic, String messageKey, Object payload) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setMessageId(messageId);
        entity.setTopic(topic);
        entity.setMessageKey(messageKey);
        entity.setPayloadType(payload.getClass().getName());
        entity.setPayload(objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
        }));
        entity.setStatus(OutboxStatus.PENDING);
        entity.setNextAttemptAt(Instant.now());
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<OutboxEventEntity> pendingBatch() {
        return repository.findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus.PENDING,
            Instant.now()
        );
    }

    @Transactional
    public void markPublished(OutboxEventEntity event) {
        OutboxEventEntity entity = repository.findById(event.getId()).orElseThrow();
        entity.setStatus(OutboxStatus.PUBLISHED);
        entity.setPublishedAt(Instant.now());
        entity.setLastError(null);
    }

    @Transactional
    public void markFailed(OutboxEventEntity event, Exception exception) {
        OutboxEventEntity entity = repository.findById(event.getId()).orElseThrow();
        entity.setAttempts(entity.getAttempts() + 1);
        entity.setStatus(OutboxStatus.PENDING);
        entity.setLastError(limit(exception.getMessage()));
        entity.setNextAttemptAt(Instant.now().plusSeconds(backoffSeconds(entity.getAttempts())));
    }

    private long backoffSeconds(int attempts) {
        return Math.min(300, Math.max(5, attempts * 10L));
    }

    private String limit(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 2000 ? message : message.substring(0, 2000);
    }
}
