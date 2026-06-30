package com.unomi.pipeline;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "processed_messages",
    uniqueConstraints = @UniqueConstraint(
        name = "ux_processed_messages_message_stage",
        columnNames = {"message_id", "stage"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class ProcessedMessageEntity {

    @Id
    private UUID id;

    @Column(name = "message_id", nullable = false, length = 80)
    private String messageId;

    @Column(nullable = false, length = 80)
    private String stage;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (processedAt == null) {
            processedAt = Instant.now();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
