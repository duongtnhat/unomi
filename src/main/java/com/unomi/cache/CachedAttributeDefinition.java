package com.unomi.cache;

import java.time.Instant;
import java.util.UUID;

import com.unomi.attribute.AttributeValueType;
import com.unomi.attribute.CustomerAttributeMergeStrategy;

public record CachedAttributeDefinition(
    UUID id,
    String key,
    String name,
    AttributeValueType type,
    Integer mergePriority,
    CustomerAttributeMergeStrategy mergeStrategy,
    Instant createdAt,
    Instant updatedAt
) {
}
