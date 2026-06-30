package com.unomi.pipeline;

import java.time.Instant;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessedMessageService {

    private final ProcessedMessageRepository repository;

    public ProcessedMessageService(ProcessedMessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public <T> T processOnce(String messageId, String stage, Supplier<T> action) {
        if (repository.existsByMessageIdAndStage(messageId, stage)) {
            return null;
        }

        T result = action.get();
        ProcessedMessageEntity entity = new ProcessedMessageEntity();
        entity.setMessageId(messageId);
        entity.setStage(stage);
        entity.setProcessedAt(Instant.now());
        repository.save(entity);
        return result;
    }

    @Transactional(readOnly = true)
    public boolean alreadyProcessed(String messageId, String stage) {
        return repository.existsByMessageIdAndStage(messageId, stage);
    }
}
