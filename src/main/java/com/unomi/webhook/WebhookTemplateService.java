package com.unomi.webhook;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.shared.NotFoundException;

@Service
public class WebhookTemplateService {

    private final WebhookTemplateRepository templateRepository;
    private final WebhookCallRepository callRepository;

    public WebhookTemplateService(
        WebhookTemplateRepository templateRepository,
        WebhookCallRepository callRepository
    ) {
        this.templateRepository = templateRepository;
        this.callRepository = callRepository;
    }

    @Transactional
    public WebhookTemplateResponse create(WebhookTemplateRequest request) {
        validate(request);
        if (templateRepository.existsByKey(request.key())) {
            throw new IllegalArgumentException("Webhook template key already exists: " + request.key());
        }
        WebhookTemplateEntity entity = new WebhookTemplateEntity();
        apply(entity, request);
        return WebhookTemplateResponse.from(templateRepository.save(entity));
    }

    @Transactional
    public WebhookTemplateResponse update(UUID id, WebhookTemplateRequest request) {
        validate(request);
        WebhookTemplateEntity entity = templateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Webhook template not found: " + id));
        templateRepository.findByKey(request.key())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Webhook template key already exists: " + request.key());
            });
        apply(entity, request);
        return WebhookTemplateResponse.from(templateRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public WebhookTemplateResponse get(UUID id) {
        return templateRepository.findById(id)
            .map(WebhookTemplateResponse::from)
            .orElseThrow(() -> new NotFoundException("Webhook template not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<WebhookTemplateResponse> list(Boolean active) {
        return templateRepository.findAllByOrderByKeyAsc()
            .stream()
            .filter(template -> active == null || template.isActive() == active)
            .map(WebhookTemplateResponse::from)
            .toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!templateRepository.existsById(id)) {
            throw new NotFoundException("Webhook template not found: " + id);
        }
        templateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<WebhookCallResponse> calls(UUID templateId) {
        return callRepository.findTop100ByTemplate_IdOrderByCreatedAtDesc(templateId)
            .stream()
            .map(WebhookCallResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<WebhookCallResponse> calls() {
        return callRepository.findTop100ByOrderByCreatedAtDesc()
            .stream()
            .map(WebhookCallResponse::from)
            .toList();
    }

    private void apply(WebhookTemplateEntity entity, WebhookTemplateRequest request) {
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setMethod(request.method().trim().toUpperCase());
        entity.setUrl(request.url());
        entity.setHeaders(new LinkedHashMap<>(nullToEmpty(request.headers())));
        entity.setBody(request.body());
        entity.setActive(request.active() == null || request.active());
    }

    private void validate(WebhookTemplateRequest request) {
        String method = request.method().trim().toUpperCase();
        if (!List.of("GET", "POST", "PUT", "PATCH", "DELETE").contains(method)) {
            throw new IllegalArgumentException("Unsupported webhook method: " + request.method());
        }
    }

    private Map<String, String> nullToEmpty(Map<String, String> value) {
        return value == null ? Map.of() : value;
    }
}
