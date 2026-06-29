package com.unomi.customer.profile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(indexName = "customer-profiles")
@Getter
@Setter
@NoArgsConstructor
public class CustomerProfileDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String profileKey;

    @Field(type = FieldType.Keyword)
    private String anonymousId;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Keyword)
    private String phoneNumber;

    @Field(type = FieldType.Object)
    private Map<String, Object> identifiers = new LinkedHashMap<>();

    @Field(type = FieldType.Object)
    private Map<String, Object> properties = new LinkedHashMap<>();

    @Field(type = FieldType.Keyword)
    private List<String> segmentIds = new ArrayList<>();

    @Field(type = FieldType.Keyword)
    private List<String> segmentKeys = new ArrayList<>();

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;
}
