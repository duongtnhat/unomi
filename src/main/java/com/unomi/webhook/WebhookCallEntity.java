package com.unomi.webhook;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "webhook_calls")
@Getter
@Setter
@NoArgsConstructor
public class WebhookCallEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private WebhookTemplateEntity template;

    @Column(name = "action_event_id")
    private UUID actionEventId;

    @Column(name = "tracking_id")
    private UUID trackingId;

    @Column(name = "message_id", length = 80)
    private String messageId;

    @Column(name = "profile_id", length = 160)
    private String profileId;

    @Column(name = "rule_key", length = 160)
    private String ruleKey;

    @Column(name = "action_key", length = 160)
    private String actionKey;

    @Column(nullable = false, length = 40)
    private String status = "PENDING";

    @Column(nullable = false, length = 16)
    private String method;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_headers", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> requestHeaders = new LinkedHashMap<>();

    @Column(name = "request_body", columnDefinition = "text")
    private String requestBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_headers", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> responseHeaders = new LinkedHashMap<>();

    @Column(name = "response_body", columnDefinition = "text")
    private String responseBody;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
