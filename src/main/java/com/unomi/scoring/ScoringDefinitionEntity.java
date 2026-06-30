package com.unomi.scoring;

import java.math.BigDecimal;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "scoring_definitions",
    uniqueConstraints = @UniqueConstraint(name = "ux_scoring_definitions_key", columnNames = "scoring_key")
)
@Getter
@Setter
@NoArgsConstructor
public class ScoringDefinitionEntity {

    @Id
    private UUID id;

    @Column(name = "scoring_key", nullable = false, length = 160)
    private String key;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private ScoringType type;

    @Column(name = "start_value", nullable = false, precision = 18, scale = 4)
    private BigDecimal startValue = BigDecimal.ZERO;

    @Column(name = "min_value", precision = 18, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "only_increase", nullable = false)
    private boolean onlyIncrease = false;

    @Column(name = "only_decrease", nullable = false)
    private boolean onlyDecrease = false;

    @Column(nullable = false)
    private boolean active = true;

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
