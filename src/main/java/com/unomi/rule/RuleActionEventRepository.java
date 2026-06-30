package com.unomi.rule;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleActionEventRepository extends JpaRepository<RuleActionEventEntity, UUID> {
}
