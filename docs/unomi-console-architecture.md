# Unomi Console Architecture

This document captures the proposed architecture for a separate web application that lets users operate Unomi Core.

## Goal

Build a standalone product named `unomi-console` outside the `unomi-core` repository.

The console should provide a user-facing admin and operations UI for profiles, events, definitions, conditions, segments, rules, scoring, actions, webhook templates, email templates, call history, and reporting.

The console must not expose the Unomi Core API key to browsers.

## Repository Direction

Use a separate repository:

```text
unomi-console/
  backend/
  frontend/
  deployment/
  docs/
```

Use a monorepo shape for the console so backend, frontend, deployment, and console-specific documentation can evolve together.

Unomi Core remains the source of truth for CDP data and processing. The console stores only admin application data such as users snapshot, audit logs, saved views, dashboard preferences, and UI settings.

## High-Level Architecture

```text
Browser
  |
  | OIDC Authorization Code + PKCE
  v
Keycloak
  |
  | access token
  v
Unomi Console Frontend
  |
  | Authorization: Bearer <accessToken>
  v
Unomi Console Backend
  |
  | X-API-Key: <internal core api key>
  v
Unomi Core API
```

The frontend calls only the console backend.

The console backend validates Keycloak JWTs and calls Unomi Core with an internal service API key.

## Recommended Stack

### Backend

Use TypeScript to speed up development and align with the frontend.

Recommended stack:

```text
NestJS
TypeScript
Fastify adapter
Prisma
PostgreSQL
Keycloak JWT validation through JWKS
OpenAPI/Swagger
Pino logger
```

NestJS gives the backend a clear modular structure. Fastify keeps HTTP overhead low. Prisma speeds up admin database CRUD. TypeScript reduces context switching between frontend and backend.

### Frontend

Recommended stack:

```text
React
TypeScript
Vite
React Router
Ant Design
TanStack Query
React Hook Form
Zod
oidc-client-ts or Keycloak JS adapter
```

Ant Design is a good fit for operational tools with many tables, filters, forms, and detail pages.

### Auth

Use Keycloak with OIDC Authorization Code + PKCE.

Do not use implicit flow.

Create a realm:

```text
realm: unomi
```

Frontend client:

```text
clientId: unomi-console-web
type: public
flow: Authorization Code + PKCE
redirectUris:
  - http://localhost:5173/*
webOrigins:
  - http://localhost:5173
```

Backend resource server:

```text
clientId: unomi-console-api
type: bearer/resource-server
```

The backend validates JWTs with Keycloak issuer and JWKS:

```text
KEYCLOAK_ISSUER_URL=http://keycloak:8080/realms/unomi
KEYCLOAK_JWKS_URI=http://keycloak:8080/realms/unomi/protocol/openid-connect/certs
```

## Backend Responsibilities

The console backend is a Backend-for-Frontend.

Responsibilities:

- Validate access tokens issued by Keycloak.
- Enforce roles and permissions.
- Keep the Unomi Core API key server-side only.
- Proxy and aggregate Unomi Core APIs for UI needs.
- Validate complex UI payloads before calling core.
- Write audit logs for every create, update, delete, publish, retry, or other mutating operation.
- Store console-specific data in a separate PostgreSQL database.
- Provide OpenAPI documentation for frontend client generation.
- Normalize errors from Unomi Core into UI-friendly responses.

The backend should not duplicate customer profiles or customer events in its own database.

## Frontend Responsibilities

The console frontend handles user workflows only.

Responsibilities:

- Login and logout through Keycloak.
- Store tokens safely for SPA usage.
- Call only the console backend.
- Hide or show UI actions based on roles.
- Never enforce security as the only layer; backend must enforce permissions.
- Provide workflow screens for profile search, event timeline, definitions, segments, rules, scoring, actions, templates, call history, and reports.

## Suggested Backend Modules

```text
backend/src/
  main.ts
  app.module.ts

  auth/
    keycloak-jwt.guard.ts
    roles.guard.ts
    current-user.decorator.ts

  config/
    env.schema.ts

  unomi-core/
    unomi-core.module.ts
    unomi-core.client.ts

  profiles/
    profiles.controller.ts
    profiles.service.ts

  events/
    events.controller.ts
    events.service.ts

  attributes/
    customer-attributes.controller.ts
    event-attributes.controller.ts

  conditions/
    conditions.controller.ts
    conditions.service.ts

  segments/
    segments.controller.ts
    segments.service.ts

  rules/
    rules.controller.ts
    rules.service.ts

  scorings/
    scorings.controller.ts
    scorings.service.ts

  actions/
    action-types.controller.ts
    webhook-templates.controller.ts
    email-templates.controller.ts

  audit/
    audit-log.entity.ts
    audit.service.ts
    audit.interceptor.ts
```

## Unomi Core Integration

The console backend calls Unomi Core through internal configuration:

```text
UNOMI_CORE_BASE_URL=http://unomi-core:8080
UNOMI_CORE_API_KEY=dev-unomi-api-key
```

All core calls include:

```http
X-API-Key: <UNOMI_CORE_API_KEY>
```

Example console backend endpoints:

```text
GET    /api/console/profiles
GET    /api/console/profiles/{id}
GET    /api/console/segments
POST   /api/console/segments
PUT    /api/console/segments/{id}
GET    /api/console/rules
POST   /api/console/rules
PUT    /api/console/rules/{id}
GET    /api/console/action-types
GET    /api/console/webhook-templates
GET    /api/console/email-templates
GET    /api/console/audit-logs
```

Console API request and response fields should use camelCase.

## Role Model

Use Keycloak roles and enforce them in the backend.

Suggested roles:

```text
UNOMI_ADMIN
UNOMI_OPERATOR
UNOMI_MARKETER
UNOMI_ANALYST
UNOMI_VIEWER
```

Suggested access:

```text
UNOMI_ADMIN
- Manage users and system-level settings.
- Full console access.

UNOMI_MARKETER
- Create and edit conditions.
- Create and edit segments.
- Create and edit rules.
- Create and edit action templates.

UNOMI_ANALYST
- View profiles.
- View events.
- View reports.

UNOMI_OPERATOR
- View action history.
- Inspect webhook and email failures.
- Retry failed operations later.

UNOMI_VIEWER
- Read-only access.
```

Frontend role checks are only for UX. Backend role checks are mandatory.

## Admin Database

Use a PostgreSQL database separate from Unomi Core.

Suggested tables:

```text
console_users_snapshot
console_audit_logs
console_saved_views
console_ui_preferences
console_dashboards
console_notification_settings
```

Example Prisma model:

```prisma
model AuditLog {
  id           String   @id @default(uuid())
  actorId      String
  actorEmail   String?
  action       String
  resourceType String
  resourceId   String?
  beforeValue  Json?
  afterValue   Json?
  ipAddress    String?
  userAgent    String?
  createdAt    DateTime @default(now())
}
```

Audit logs should capture all mutating operations, especially changes to segments, rules, scoring, action types, webhook templates, email templates, and operational retries.

## Frontend And Backend Contract

Recommended initial approach:

```text
Backend generates OpenAPI
Frontend generates a TypeScript API client
```

This keeps backend and frontend decoupled while still providing type safety.

A later option is to add a shared `packages/contracts` package with Zod schemas if both sides need stronger shared validation.

## Local Deployment

The console repository should include local Docker Compose for:

```text
keycloak
console-postgres
console-backend
console-frontend
```

Unomi Core can keep its own Docker Compose.

For local integration, both stacks can join a shared Docker network:

```bash
docker network create unomi-platform
```

Then run core and console services on the same network so `unomi-console-backend` can call `unomi-core`.

## Production Deployment

Deploy as separate services:

```text
keycloak
unomi-core-api
unomi-core-workers
unomi-console-backend
unomi-console-frontend
postgres-core
postgres-console
elasticsearch
kafka
redis
```

The frontend can be served as static files through Nginx or another web server.

The backend runs as a NestJS container.

## Security Rules

- Never expose `UNOMI_CORE_API_KEY` to the browser.
- Use Authorization Code + PKCE for browser login.
- Validate JWTs on the console backend using Keycloak issuer and JWKS.
- Enforce permissions in the backend.
- Restrict CORS to known frontend origins.
- Log audit records for every mutating console operation.
- Keep customer profile and event data in Unomi Core, not in the console database.
- Use internal service networking for console backend to core communication.

## Delivery Roadmap

### Phase 1

- Create `unomi-console` repository.
- Scaffold NestJS backend.
- Scaffold React frontend.
- Add Keycloak local Docker Compose.
- Add `/health` endpoint.
- Add `/me` endpoint returning user identity and roles.
- Implement frontend login/logout.
- Verify backend JWT validation.

### Phase 2

- Implement `UnomiCoreClient`.
- Add attribute definition screens.
- Add condition, segment, and rule CRUD screens.
- Add audit log for mutating APIs.

### Phase 3

- Add profile search and profile detail.
- Add event timeline.
- Add action type UI.
- Add webhook template and call history UI.
- Add email SMTP/template and call history UI.

### Phase 4

- Add visual condition builder.
- Add visual rule builder.
- Add segment preview.
- Add reporting dashboard.
- Add retry workflows for failed webhook, email, and action processing.

## Decision

Use a separate `unomi-console` repository with:

```text
Backend: NestJS + TypeScript + Fastify + Prisma + PostgreSQL
Frontend: React + TypeScript + Vite + Ant Design
Auth: Keycloak OIDC Authorization Code + PKCE
Core integration: console backend calls Unomi Core with an internal API key
```

This direction prioritizes fast development, strong frontend/backend alignment, clear security boundaries, and independent deployment from Unomi Core.
