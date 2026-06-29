package com.unomi.customer.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unomi.attribute.AttributeDefinitionService;

@Service
public class CustomerEventService {

    private final CustomerEventRepository repository;
    private final AttributeDefinitionService attributeDefinitionService;

    public CustomerEventService(
        CustomerEventRepository repository,
        AttributeDefinitionService attributeDefinitionService
    ) {
        this.repository = repository;
        this.attributeDefinitionService = attributeDefinitionService;
    }

    public CustomerEventResponse ingest(CustomerEventRequest request) {
        Instant now = Instant.now();
        CustomerEventDocument event = new CustomerEventDocument();
        event.setId(UUID.randomUUID().toString());
        event.setProfileId(request.profileId());
        event.setEventType(request.eventType());
        event.setSource(request.source());
        event.setPayload(attributeDefinitionService.filterEventAttributes(request.payload()));
        event.setOccurredAt(request.occurredAt() == null ? now : request.occurredAt());
        event.setReceivedAt(now);

        return CustomerEventResponse.from(repository.save(event));
    }

    public List<CustomerEventResponse> listByProfile(String profileId) {
        return repository.findTop50ByProfileIdOrderByOccurredAtDesc(profileId)
            .stream()
            .map(CustomerEventResponse::from)
            .toList();
    }
}
