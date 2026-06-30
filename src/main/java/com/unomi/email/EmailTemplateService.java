package com.unomi.email;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.shared.NotFoundException;

@Service
public class EmailTemplateService {

    private final EmailTemplateRepository templateRepository;
    private final EmailSmtpConfigRepository smtpConfigRepository;
    private final EmailCallRepository callRepository;

    public EmailTemplateService(
        EmailTemplateRepository templateRepository,
        EmailSmtpConfigRepository smtpConfigRepository,
        EmailCallRepository callRepository
    ) {
        this.templateRepository = templateRepository;
        this.smtpConfigRepository = smtpConfigRepository;
        this.callRepository = callRepository;
    }

    @Transactional
    public EmailTemplateResponse create(EmailTemplateRequest request) {
        if (templateRepository.existsByKey(request.key())) {
            throw new IllegalArgumentException("Email template key already exists: " + request.key());
        }
        EmailTemplateEntity entity = new EmailTemplateEntity();
        apply(entity, request);
        return EmailTemplateResponse.from(templateRepository.save(entity));
    }

    @Transactional
    public EmailTemplateResponse update(UUID id, EmailTemplateRequest request) {
        EmailTemplateEntity entity = templateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Email template not found: " + id));
        templateRepository.findByKey(request.key())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Email template key already exists: " + request.key());
            });
        apply(entity, request);
        return EmailTemplateResponse.from(templateRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse get(UUID id) {
        return templateRepository.findById(id)
            .map(EmailTemplateResponse::from)
            .orElseThrow(() -> new NotFoundException("Email template not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> list(Boolean active) {
        return templateRepository.findAllByOrderByKeyAsc()
            .stream()
            .filter(template -> active == null || template.isActive() == active)
            .map(EmailTemplateResponse::from)
            .toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!templateRepository.existsById(id)) {
            throw new NotFoundException("Email template not found: " + id);
        }
        templateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EmailCallResponse> calls(UUID templateId) {
        return callRepository.findTop100ByTemplate_IdOrderByCreatedAtDesc(templateId)
            .stream()
            .map(EmailCallResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EmailCallResponse> calls() {
        return callRepository.findTop100ByOrderByCreatedAtDesc()
            .stream()
            .map(EmailCallResponse::from)
            .toList();
    }

    private void apply(EmailTemplateEntity entity, EmailTemplateRequest request) {
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setSmtpConfig(smtpConfigRepository.findById(request.smtpConfigId())
            .orElseThrow(() -> new NotFoundException("SMTP config not found: " + request.smtpConfigId())));
        entity.setToAddress(request.toAddress());
        entity.setSubject(request.subject());
        entity.setBody(request.body());
        entity.setContentType(request.contentType() == null || request.contentType().isBlank()
            ? "text/html"
            : request.contentType());
        entity.setActive(request.active() == null || request.active());
    }
}
