package com.unomi.email;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "email_smtp_configs",
    uniqueConstraints = @UniqueConstraint(name = "ux_email_smtp_configs_key", columnNames = "config_key")
)
@Getter
@Setter
@NoArgsConstructor
public class EmailSmtpConfigEntity {

    @Id
    private UUID id;

    @Column(name = "config_key", nullable = false, length = 160)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port;

    private String username;

    @Column(columnDefinition = "text")
    private String password;

    @Column(name = "from_address", nullable = false, length = 320)
    private String fromAddress;

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "auth_enabled", nullable = false)
    private boolean authEnabled = true;

    @Column(name = "start_tls_enabled", nullable = false)
    private boolean startTlsEnabled = true;

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
