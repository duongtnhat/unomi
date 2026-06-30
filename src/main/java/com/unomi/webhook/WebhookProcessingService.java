package com.unomi.webhook;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.samskivert.mustache.Mustache;
import com.unomi.action.messaging.ActionExecutionCommand;

@Service
public class WebhookProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookProcessingService.class);

    private final WebhookTemplateRepository templateRepository;
    private final WebhookCallRepository callRepository;
    private final RestClient restClient;

    public WebhookProcessingService(
        WebhookTemplateRepository templateRepository,
        WebhookCallRepository callRepository,
        RestClient.Builder restClientBuilder
    ) {
        this.templateRepository = templateRepository;
        this.callRepository = callRepository;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
    }

    public WebhookCallEntity process(ActionExecutionCommand command) {
        String templateKey = templateKey(command);
        if (templateKey == null) {
            LOGGER.warn("Webhook action {} does not contain payload.template", command.actionEventId());
            return null;
        }

        WebhookTemplateEntity template = templateRepository.findByKeyAndActiveTrue(templateKey)
            .orElse(null);
        if (template == null) {
            LOGGER.warn("Active webhook template not found for key {}", templateKey);
            return null;
        }

        Map<String, Object> context = context(command);
        String requestBody = render(template.getBody(), context);
        Map<String, Object> requestHeaders = new LinkedHashMap<>(template.getHeaders());

        WebhookCallEntity call = new WebhookCallEntity();
        call.setTemplate(template);
        call.setActionEventId(command.actionEventId());
        call.setTrackingId(trackingId(command));
        call.setMessageId(command.messageId());
        call.setProfileId(command.profileId());
        call.setRuleKey(command.ruleKey());
        call.setActionKey(command.actionKey());
        call.setMethod(template.getMethod());
        call.setUrl(template.getUrl());
        call.setRequestHeaders(requestHeaders);
        call.setRequestBody(requestBody);
        call = callRepository.save(call);

        try {
            ResponseEntity<String> response = restClient
                .method(HttpMethod.valueOf(template.getMethod()))
                .uri(template.getUrl())
                .headers(headers -> template.getHeaders().forEach(headers::add))
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);
            call.setStatus("SUCCESS");
            call.setResponseStatus(response.getStatusCode().value());
            call.setResponseHeaders(headers(response.getHeaders().toSingleValueMap()));
            call.setResponseBody(response.getBody());
            LOGGER.info(
                "Webhook call succeeded actionEventId={} trackingId={} profileId={} templateKey={} status={}",
                command.actionEventId(),
                trackingId(command),
                command.profileId(),
                template.getKey(),
                response.getStatusCode().value()
            );
        } catch (RestClientResponseException exception) {
            call.setStatus("FAILED");
            call.setResponseStatus(exception.getStatusCode().value());
            call.setResponseHeaders(headers(exception.getResponseHeaders() == null
                ? Map.of()
                : exception.getResponseHeaders().toSingleValueMap()));
            call.setResponseBody(exception.getResponseBodyAsString(StandardCharsets.UTF_8));
            call.setErrorMessage(limit(exception.getMessage()));
            LOGGER.warn(
                "Webhook call failed actionEventId={} trackingId={} profileId={} templateKey={} status={}",
                command.actionEventId(),
                trackingId(command),
                command.profileId(),
                template.getKey(),
                exception.getStatusCode().value()
            );
        } catch (Exception exception) {
            call.setStatus("FAILED");
            call.setErrorMessage(limit(exception.getMessage()));
            LOGGER.warn(
                "Webhook call failed actionEventId={} trackingId={} profileId={} templateKey={}",
                command.actionEventId(),
                trackingId(command),
                command.profileId(),
                template.getKey(),
                exception
            );
        }

        call.setCompletedAt(Instant.now());
        return callRepository.save(call);
    }

    private String templateKey(ActionExecutionCommand command) {
        Object value = command.payload() == null ? null : command.payload().get("template");
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        return null;
    }

    private java.util.UUID trackingId(ActionExecutionCommand command) {
        return command.trackingId() == null ? command.actionEventId() : command.trackingId();
    }

    private String render(String template, Map<String, Object> context) {
        return Mustache.compiler()
            .escapeHTML(false)
            .compile(template)
            .execute(context);
    }

    private Map<String, Object> context(ActionExecutionCommand command) {
        Map<String, Object> context = new LinkedHashMap<>();
        if (command.payload() != null) {
            context.putAll(command.payload());
        }
        context.put("payload", command.payload() == null ? Map.of() : command.payload());
        context.put("actionEventId", command.actionEventId().toString());
        context.put("trackingId", trackingId(command).toString());
        context.put("messageId", command.messageId());
        context.put("requestedAt", command.requestedAt());
        context.put("profileId", command.profileId());
        context.put("ruleKey", command.ruleKey());
        context.put("actionKey", command.actionKey());
        context.put("actionType", command.actionType());
        return context;
    }

    private Map<String, Object> headers(Map<String, String> values) {
        return new LinkedHashMap<>(values);
    }

    private String limit(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 2000 ? message : message.substring(0, 2000);
    }
}
