# Condition Building Guide

This document explains how to build condition trees used by conditions, segments, rules, and profile search.

All API payload fields use camelCase.

## Condition Shape

A condition is a tree node:

```json
{
  "type": "profileProperty",
  "parameters": {
    "propertyName": "properties.age",
    "operator": "gte",
    "value": 18
  },
  "conditions": []
}
```

Fields:

```text
type        Condition type.
parameters  Type-specific parameters.
conditions  Child conditions used by boolean conditions.
```

## Supported Condition Types

### boolean

Combines child conditions.

Supported operators:

```text
and
or
not
```

Example:

```json
{
  "type": "boolean",
  "parameters": {
    "operator": "and"
  },
  "conditions": [
    {
      "type": "profileProperty",
      "parameters": {
        "propertyName": "properties.age",
        "operator": "gte",
        "value": 18
      }
    },
    {
      "type": "profileProperty",
      "parameters": {
        "propertyName": "tags",
        "operator": "contains",
        "value": "vip"
      }
    }
  ]
}
```

### profileProperty

Evaluates a field from the profile context.

Common profile paths:

```text
id
profileKey
anonymousId
email
phoneNumber
identifiers.<key>
properties.<key>
segmentIds
segmentKeys
tags
scores.<scoreKey>
createdAt
updatedAt
```

Example:

```json
{
  "type": "profileProperty",
  "parameters": {
    "propertyName": "properties.lifetimeValue",
    "operator": "gte",
    "value": 1000
  }
}
```

### profileId

Compares the profile ID.

Example:

```json
{
  "type": "profileId",
  "parameters": {
    "operator": "equals",
    "value": "profile-1"
  }
}
```

### exists

Checks whether a path exists. For profile search, use `target: "profile"`.

Example:

```json
{
  "type": "exists",
  "parameters": {
    "target": "profile",
    "propertyName": "properties.emailOptIn"
  }
}
```

### eventProperty And eventType

These are used by segment and rule evaluation when an incoming event context is available.

Profile search does not have an event context, so event-only conditions will not match profiles unless they are part of a boolean tree where another profile condition can satisfy the tree.

## Supported Operators

```text
equals
notEquals
contains
in
gt
gte
lt
lte
exists
missing
```

Operator behavior:

```text
equals     Normalized equality. Numbers and datetimes are normalized before comparison.
notEquals  Opposite of equals.
contains   Works for arrays and strings.
in         The expected value must be an array containing the actual value.
gt         Greater than. Supports numbers, datetimes, and string fallback.
gte        Greater than or equal.
lt         Less than.
lte        Less than or equal.
exists     Actual value is not null.
missing    Actual value is null.
```

Datetime values should be ISO-8601 strings, for example:

```text
2026-07-01T00:00:00Z
```

## Search Profiles By Condition

Endpoint:

```http
POST /api/profiles/search?page=0&size=20
X-API-Key: dev-unomi-api-key
Content-Type: application/json
```

Request:

```json
{
  "condition": {
    "type": "boolean",
    "parameters": {
      "operator": "and"
    },
    "conditions": [
      {
        "type": "profileProperty",
        "parameters": {
          "propertyName": "properties.lifetimeValue",
          "operator": "gte",
          "value": 1000
        }
      },
      {
        "type": "profileProperty",
        "parameters": {
          "propertyName": "tags",
          "operator": "contains",
          "value": "vip"
        }
      }
    ]
  }
}
```

Response:

```json
{
  "content": [
    {
      "id": "profile-1",
      "profileKey": "email:ada@example.com",
      "anonymousId": "anon-123",
      "email": "ada@example.com",
      "properties": {
        "lifetimeValue": 1200
      },
      "segmentIds": [],
      "segmentKeys": [],
      "tags": ["vip"],
      "scores": {},
      "createdAt": "2026-07-01T00:00:00Z",
      "updatedAt": "2026-07-01T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

`page` is zero-based.

`size` is clamped to the range `1..200`.

## Profile Search Examples

Find profiles by email:

```json
{
  "condition": {
    "type": "profileProperty",
    "parameters": {
      "propertyName": "email",
      "operator": "equals",
      "value": "ada@example.com"
    }
  }
}
```

Find profiles in a segment:

```json
{
  "condition": {
    "type": "profileProperty",
    "parameters": {
      "propertyName": "segmentKeys",
      "operator": "contains",
      "value": "adultBuyers"
    }
  }
}
```

Find profiles with a score:

```json
{
  "condition": {
    "type": "profileProperty",
    "parameters": {
      "propertyName": "scores.engagement",
      "operator": "gte",
      "value": 50
    }
  }
}
```

Find profiles that have a PII email attribute:

```json
{
  "condition": {
    "type": "exists",
    "parameters": {
      "target": "profile",
      "propertyName": "email"
    }
  }
}
```

## Notes

Profile search currently evaluates conditions with the same in-memory evaluator used by rule and segment logic. This keeps matching behavior consistent across the system.

For very large profile indexes, a later optimization can translate profile-only conditions into native Elasticsearch queries while keeping this API contract unchanged.
