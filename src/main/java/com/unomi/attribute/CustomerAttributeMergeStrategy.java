package com.unomi.attribute;

public enum CustomerAttributeMergeStrategy {
    SOURCE_PRIORITY,
    NEWEST_VALUE,
    OLDEST_VALUE,
    MAX_VALUE,
    MIN_VALUE,
    SUM,
    UNION
}
