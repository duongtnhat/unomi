package com.unomi.email;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.shared.NotFoundException;

@Service
public class EmailSmtpConfigService {

    private final EmailSmtpConfigRepository repository;
    private final EmailCallRepository callRepository;

    public EmailSmtpConfigService(
        EmailSmtpConfigRepository repository,
        EmailCallRepository callRepository
    ) {
        this.repository = repository;
        this.callRepository = callRepository;
    }

    @Transactional
    public EmailSmtpConfigResponse create(EmailSmtpConfigRequest request) {
        if (repository.existsByKey(request.key())) {
            throw new IllegalArgumentException("SMTP config key already exists: " + request.key());
        }
        EmailSmtpConfigEntity entity = new EmailSmtpConfigEntity();
        apply(entity, request);
        return EmailSmtpConfigResponse.from(repository.save(entity));
    }

    @Transactional
    public EmailSmtpConfigResponse update(UUID id, EmailSmtpConfigRequest request) {
        EmailSmtpConfigEntity entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("SMTP config not found: " + id));
        repository.findByKey(request.key())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("SMTP config key already exists: " + request.key());
            });
        apply(entity, request);
        return EmailSmtpConfigResponse.from(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public EmailSmtpConfigResponse get(UUID id) {
        return repository.findById(id)
            .map(EmailSmtpConfigResponse::from)
            .orElseThrow(() -> new NotFoundException("SMTP config not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<EmailSmtpConfigResponse> list(Boolean active) {
        return repository.findAllByOrderByKeyAsc()
            .stream()
            .filter(config -> active == null || config.isActive() == active)
            .map(EmailSmtpConfigResponse::from)
            .toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("SMTP config not found: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EmailCallResponse> calls(UUID smtpConfigId) {
        return callRepository.findTop100BySmtpConfig_IdOrderByCreatedAtDesc(smtpConfigId)
            .stream()
            .map(EmailCallResponse::from)
            .toList();
    }

    private void apply(EmailSmtpConfigEntity entity, EmailSmtpConfigRequest request) {
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setHost(request.host());
        entity.setPort(request.port());
        entity.setUsername(request.username());
        entity.setPassword(request.password());
        entity.setFromAddress(request.fromAddress());
        entity.setFromName(request.fromName());
        entity.setAuthEnabled(request.authEnabled() == null || request.authEnabled());
        entity.setStartTlsEnabled(request.startTlsEnabled() == null || request.startTlsEnabled());
        entity.setActive(request.active() == null || request.active());
    }
}
