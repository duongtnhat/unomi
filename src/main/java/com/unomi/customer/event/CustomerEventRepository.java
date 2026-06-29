package com.unomi.customer.event;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomerEventRepository extends ElasticsearchRepository<CustomerEventDocument, String> {

    List<CustomerEventDocument> findTop50ByProfileIdOrderByOccurredAtDesc(String profileId);
}
