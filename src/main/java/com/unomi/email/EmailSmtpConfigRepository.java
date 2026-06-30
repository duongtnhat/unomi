package com.unomi.email;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSmtpConfigRepository extends JpaRepository<EmailSmtpConfigEntity, UUID> {

    List<EmailSmtpConfigEntity> findAllByOrderByKeyAsc();

    boolean existsByKey(String key);

    Optional<EmailSmtpConfigEntity> findByKey(String key);
}
