package com.unomi.attribute;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_attribute_definitions")
@Getter
@Setter
@NoArgsConstructor
public class CustomerAttributeDefinitionEntity {

    @Id
    private UUID id;

    @Column(name = "attribute_key", nullable = false, unique = true, length = 160)
    private String key;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 40)
    private AttributeValueType type;

    @Column(name = "merge_priority")
    private Integer mergePriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "merge_strategy", length = 60)
    private CustomerAttributeMergeStrategy mergeStrategy;

    @Column(nullable = false)
    private boolean pii = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
