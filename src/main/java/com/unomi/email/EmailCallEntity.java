package com.unomi.email;

import java.time.Instant;
import java.util.UUID;

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
@Table(name = "email_calls")
@Getter
@Setter
@NoArgsConstructor
public class EmailCallEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private EmailTemplateEntity template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "smtp_config_id", nullable = false)
    private EmailSmtpConfigEntity smtpConfig;

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

    @Column(name = "from_address", nullable = false, length = 320)
    private String fromAddress;

    @Column(name = "to_address", columnDefinition = "text")
    private String toAddress;

    @Column(nullable = false, columnDefinition = "text")
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

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
