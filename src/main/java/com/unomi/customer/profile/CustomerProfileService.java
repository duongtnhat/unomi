package com.unomi.customer.profile;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unomi.attribute.AttributeDefinitionService;
import com.unomi.shared.NotFoundException;

@Service
public class CustomerProfileService {

    private final CustomerProfileRepository repository;
    private final AttributeDefinitionService attributeDefinitionService;

    public CustomerProfileService(
        CustomerProfileRepository repository,
        AttributeDefinitionService attributeDefinitionService
    ) {
        this.repository = repository;
        this.attributeDefinitionService = attributeDefinitionService;
    }

    public CustomerProfileResponse upsert(CustomerProfileRequest request) {
        Instant now = Instant.now();
        CustomerProfileDocument profile = repository.findByProfileKey(request.profileKey())
            .orElseGet(CustomerProfileDocument::new);

        if (profile.getId() == null) {
            profile.setId(UUID.randomUUID().toString());
            profile.setCreatedAt(now);
        }
        profile.setProfileKey(request.profileKey());
        profile.setAnonymousId(request.anonymousId());
        profile.setEmail(request.email());
        profile.setProperties(attributeDefinitionService.filterCustomerAttributes(request.properties()));
        profile.setUpdatedAt(now);

        return CustomerProfileResponse.from(repository.save(profile));
    }

    public CustomerProfileResponse get(String id) {
        return repository.findById(id)
            .map(CustomerProfileResponse::from)
            .orElseThrow(() -> new NotFoundException("Customer profile not found: " + id));
    }
}
