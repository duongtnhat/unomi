package com.unomi.customer.profile;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomerProfileRepository extends ElasticsearchRepository<CustomerProfileDocument, String> {

    Optional<CustomerProfileDocument> findByProfileKey(String profileKey);

    Optional<CustomerProfileDocument> findByEmail(String email);

    Optional<CustomerProfileDocument> findByPhoneNumber(String phoneNumber);
}
