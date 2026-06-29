package com.unomi.attribute;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.cache.CachedAttributeDefinition;
import com.unomi.cache.MetadataCacheService;
import com.unomi.shared.NotFoundException;

@Service
public class AttributeDefinitionService {

    private final CustomerAttributeDefinitionRepository customerRepository;
    private final EventAttributeDefinitionRepository eventRepository;
    private final MetadataCacheService cacheService;

    public AttributeDefinitionService(
        CustomerAttributeDefinitionRepository customerRepository,
        EventAttributeDefinitionRepository eventRepository,
        MetadataCacheService cacheService
    ) {
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.cacheService = cacheService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterCustomerAttributes(Map<String, Object> attributes) {
        Map<String, CachedAttributeDefinition> definitions = new LinkedHashMap<>();
        getCustomerDefinitions().forEach(definition -> definitions.put(definition.key(), definition));
        return filter(attributes, definitions);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterEventAttributes(Map<String, Object> attributes) {
        Map<String, CachedAttributeDefinition> definitions = new LinkedHashMap<>();
        getEventDefinitions().forEach(definition -> definitions.put(definition.key(), definition));
        return filter(attributes, definitions);
    }

    @Transactional(readOnly = true)
    public List<CachedAttributeDefinition> getCustomerDefinitions() {
        return cacheService.getCustomerAttributes()
            .orElseGet(this::refreshCustomerDefinitionsCache);
    }

    @Transactional(readOnly = true)
    public List<CachedAttributeDefinition> getEventDefinitions() {
        return cacheService.getEventAttributes()
            .orElseGet(this::refreshEventDefinitionsCache);
    }

    @Transactional
    public AttributeDefinitionResponse createCustomer(AttributeDefinitionRequest request) {
        if (customerRepository.existsByKey(request.key())) {
            throw new IllegalArgumentException("Customer attribute key already exists: " + request.key());
        }
        CustomerAttributeDefinitionEntity entity = new CustomerAttributeDefinitionEntity();
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setType(request.type());
        entity.setMergePriority(request.mergePriority());
        entity.setMergeStrategy(request.mergeStrategy());
        AttributeDefinitionResponse response = AttributeDefinitionResponse.from(customerRepository.save(entity));
        refreshCustomerDefinitionsCache();
        return response;
    }

    @Transactional
    public AttributeDefinitionResponse updateCustomer(UUID id, AttributeDefinitionRequest request) {
        CustomerAttributeDefinitionEntity entity = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer attribute definition not found: " + id));
        customerRepository.findByKey(request.key())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Customer attribute key already exists: " + request.key());
            });
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setType(request.type());
        entity.setMergePriority(request.mergePriority());
        entity.setMergeStrategy(request.mergeStrategy());
        AttributeDefinitionResponse response = AttributeDefinitionResponse.from(customerRepository.save(entity));
        refreshCustomerDefinitionsCache();
        return response;
    }

    @Transactional(readOnly = true)
    public AttributeDefinitionResponse getCustomer(UUID id) {
        return customerRepository.findById(id)
            .map(AttributeDefinitionResponse::from)
            .orElseThrow(() -> new NotFoundException("Customer attribute definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<AttributeDefinitionResponse> listCustomers() {
        return getCustomerDefinitions()
            .stream()
            .map(definition -> new AttributeDefinitionResponse(
                definition.id(),
                definition.key(),
                definition.name(),
                definition.type(),
                definition.mergePriority(),
                definition.mergeStrategy(),
                definition.createdAt(),
                definition.updatedAt()
            ))
            .toList();
    }

    @Transactional
    public void deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new NotFoundException("Customer attribute definition not found: " + id);
        }
        customerRepository.deleteById(id);
        refreshCustomerDefinitionsCache();
    }

    @Transactional
    public AttributeDefinitionResponse createEvent(AttributeDefinitionRequest request) {
        if (eventRepository.existsByKey(request.key())) {
            throw new IllegalArgumentException("Event attribute key already exists: " + request.key());
        }
        EventAttributeDefinitionEntity entity = new EventAttributeDefinitionEntity();
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setType(request.type());
        AttributeDefinitionResponse response = AttributeDefinitionResponse.from(eventRepository.save(entity));
        refreshEventDefinitionsCache();
        return response;
    }

    @Transactional
    public AttributeDefinitionResponse updateEvent(UUID id, AttributeDefinitionRequest request) {
        EventAttributeDefinitionEntity entity = eventRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Event attribute definition not found: " + id));
        eventRepository.findByKey(request.key())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Event attribute key already exists: " + request.key());
            });
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setType(request.type());
        AttributeDefinitionResponse response = AttributeDefinitionResponse.from(eventRepository.save(entity));
        refreshEventDefinitionsCache();
        return response;
    }

    @Transactional(readOnly = true)
    public AttributeDefinitionResponse getEvent(UUID id) {
        return eventRepository.findById(id)
            .map(AttributeDefinitionResponse::from)
            .orElseThrow(() -> new NotFoundException("Event attribute definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<AttributeDefinitionResponse> listEvents() {
        return getEventDefinitions()
            .stream()
            .map(definition -> new AttributeDefinitionResponse(
                definition.id(),
                definition.key(),
                definition.name(),
                definition.type(),
                null,
                null,
                definition.createdAt(),
                definition.updatedAt()
            ))
            .toList();
    }

    @Transactional
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new NotFoundException("Event attribute definition not found: " + id);
        }
        eventRepository.deleteById(id);
        refreshEventDefinitionsCache();
    }

    private Map<String, Object> filter(Map<String, Object> attributes, Map<String, ?> definitions) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            Object definition = definitions.get(attribute.getKey());
            if (definition == null) {
                continue;
            }

            AttributeValueType type = ((CachedAttributeDefinition) definition).type();
            if (matchesType(type, attribute.getValue())) {
                filtered.put(attribute.getKey(), attribute.getValue());
            }
        }
        return filtered;
    }

    private List<CachedAttributeDefinition> refreshCustomerDefinitionsCache() {
        List<CachedAttributeDefinition> definitions = customerRepository.findAll()
            .stream()
            .map(entity -> new CachedAttributeDefinition(
                entity.getId(),
                entity.getKey(),
                entity.getName(),
                entity.getType(),
                entity.getMergePriority(),
                entity.getMergeStrategy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
            ))
            .sorted(Comparator.comparing(CachedAttributeDefinition::key))
            .toList();
        cacheService.putCustomerAttributes(definitions);
        return definitions;
    }

    private List<CachedAttributeDefinition> refreshEventDefinitionsCache() {
        List<CachedAttributeDefinition> definitions = eventRepository.findAll()
            .stream()
            .map(entity -> new CachedAttributeDefinition(
                entity.getId(),
                entity.getKey(),
                entity.getName(),
                entity.getType(),
                null,
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
            ))
            .sorted(Comparator.comparing(CachedAttributeDefinition::key))
            .toList();
        cacheService.putEventAttributes(definitions);
        return definitions;
    }

    private boolean matchesType(AttributeValueType type, Object value) {
        return switch (type) {
            case TEXT -> value instanceof String;
            case NUMBER -> value instanceof Number;
            case DATETIME -> isDatetime(value);
            case LIST_OF_TEXT -> isListOf(value, String.class);
            case LIST_OF_NUMBER -> isListOf(value, Number.class);
        };
    }

    private boolean isDatetime(Object value) {
        if (!(value instanceof String text)) {
            return value instanceof Instant;
        }
        try {
            OffsetDateTime.parse(text);
            return true;
        } catch (DateTimeParseException exception) {
            try {
                Instant.parse(text);
                return true;
            } catch (DateTimeParseException ignored) {
                return false;
            }
        }
    }

    private boolean isListOf(Object value, Class<?> itemType) {
        if (!(value instanceof Collection<?> collection)) {
            return false;
        }
        return collection.stream().allMatch(itemType::isInstance);
    }
}
