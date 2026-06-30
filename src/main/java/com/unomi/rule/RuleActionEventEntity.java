package com.unomi.rule;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rule_action_events")
@Getter
@Setter
@NoArgsConstructor
public class RuleActionEventEntity {

    @Id
    private UUID id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "rule_key", nullable = false, length = 160)
    private String ruleKey;

    @Column(name = "message_id", nullable = false, length = 80)
    private String messageId;

    @Column(name = "profile_id", nullable = false, length = 160)
    private String profileId;

    @Column(name = "tracking_id")
    private UUID trackingId;

    @Column(name = "action_key", nullable = false, length = 160)
    private String actionKey;

    @Column(name = "action_type", nullable = false, length = 80)
    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload = new LinkedHashMap<>();

    @Column(nullable = false, length = 40)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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
