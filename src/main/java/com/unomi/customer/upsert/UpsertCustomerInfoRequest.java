package com.unomi.customer.upsert;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Batch upsert request for customer attributes and events.")
public record UpsertCustomerInfoRequest(
    @Schema(description = "Reserved for future hook processing. Currently accepted but not used.", example = "false")
    Boolean skipHook,
    @Schema(description = "Optional callback URL for future async processing errors.", example = "https://example.com/unomi/errors")
    String errorCallbackEndpoint,
    @Schema(description = "Users to insert or update. Maximum 1000 records per request.")
    List<UpsertUserRequest> users
) {
}
