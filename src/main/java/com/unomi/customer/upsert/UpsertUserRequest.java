package com.unomi.customer.upsert;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One customer profile update with optional events.")
public record UpsertUserRequest(
    @Schema(description = "Internal profile ID. If present, it is used as the strongest identifier.", example = "5b412499-dfbd-43f1-8db1-ceeb6185ecd2")
    String insiderId,
    @Schema(description = "Customer identifiers. Supported first-class keys include email, uuid, phoneNumber, and custom.", example = "{\"email\":\"ada@example.com\",\"uuid\":\"customer-001\",\"phoneNumber\":\"+84901234567\"}")
    Map<String, Object> identifiers,
    @Schema(description = "Attributes to merge into the customer profile. Unknown keys or values with the wrong type are ignored.", example = "{\"age\":30,\"language\":\"en_US\",\"favoriteColor\":[\"green\"]}")
    Map<String, Object> attributes,
    @Schema(description = "Events to ingest for this customer.")
    List<UpsertEventRequest> events,
    @Schema(description = "When true, array attributes are appended. Takes priority over notAppend.", example = "true")
    Boolean append,
    @Schema(description = "When true and append is omitted, incoming array attributes overwrite existing values.", example = "false")
    Boolean notAppend
) {
}
