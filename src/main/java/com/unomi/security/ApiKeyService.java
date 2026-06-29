package com.unomi.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ApiKeyService {

    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Optional<ApiKeyEntity> authenticate(String rawApiKey) {
        if (!StringUtils.hasText(rawApiKey)) {
            return Optional.empty();
        }

        Optional<ApiKeyEntity> key = repository.findByKeyHash(sha256(rawApiKey));
        if (key.isEmpty() || !isUsable(key.get())) {
            return Optional.empty();
        }

        key.get().setLastUsedAt(Instant.now());
        return key;
    }

    private boolean isUsable(ApiKeyEntity key) {
        return key.isActive() && (key.getExpiresAt() == null || key.getExpiresAt().isAfter(Instant.now()));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
