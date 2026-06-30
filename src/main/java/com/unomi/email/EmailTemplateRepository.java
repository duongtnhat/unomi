package com.unomi.email;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplateEntity, UUID> {

    List<EmailTemplateEntity> findAllByOrderByKeyAsc();

    boolean existsByKey(String key);

    Optional<EmailTemplateEntity> findByKey(String key);

    Optional<EmailTemplateEntity> findByKeyAndActiveTrue(String key);
}
