package com.unomi.customer.event;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(indexName = "customer-events")
@Getter
@Setter
@NoArgsConstructor
public class CustomerEventDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String profileId;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Keyword)
    private String source;

    @Field(type = FieldType.Object)
    private Map<String, Object> payload = new LinkedHashMap<>();

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant occurredAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant receivedAt;
}
