package com.unomi.pipeline;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class InboxService {

    private final InboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public InboxService(InboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void record(String messageId, String source, Object payload, Instant receivedAt) {
        if (repository.existsByMessageId(messageId)) {
            return;
        }

        InboxEventEntity entity = new InboxEventEntity();
        entity.setMessageId(messageId);
        entity.setSource(source);
        entity.setPayloadType(payload.getClass().getName());
        entity.setPayload(objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
        }));
        entity.setReceivedAt(receivedAt);
        repository.save(entity);
    }
}
