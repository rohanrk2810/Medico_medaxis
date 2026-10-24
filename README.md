# Medical ERP

A1 foundation started. This is an incomplete development scaffold, not a working ERP.

## Current code
Angular standalone shell with Material cards, lazy routing and correlation interceptor. Separate Java 21 Spring Boot identity, tenant and audit applications with service-owned Flyway migrations. Login, refresh, logout, session identity, JWKS and permission-scoped tenant configuration endpoints are implemented. Other business endpoints remain denied or unimplemented.

## Local setup
Web: `cd apps/web-angular`, `npm install`, `npm run build`, `npm start`.
Backend: install JDK 21, run `./mvnw verify` at project root. For each service configure DATABASE_URL, DATABASE_USER and DATABASE_PASSWORD against a separate PostgreSQL database, then run its packaged jar. Default ports: identity 8081, tenant 8082, audit 8083. Use separate migration and runtime credentials before deployment.

## Verification status
Angular `npm install` and `npm run build` passed. Maven `verify` passed for identity, tenant, audit and gateway with Java 21 JDK; four tenant-scope unit tests passed. Maven wrapper and npm lockfile are included. Identity migrations and HTTP login/session flows have PostgreSQL integration coverage. Browser E2E and cross-service deployment remain pending. See docs/verification/2026-10-24.md.

Tenant read API requires IDENTITY_ISSUER_URI and a signed JWT with audience `medical-erp`, `tenant_id` and `scope: tenant:read`. Identity publishes public signing keys at /api/v1/auth/jwks; configure IDENTITY_JWKS_URI for tenant-service. Gateway runs on port 8080. Other endpoints remain denied by default.

## Next A1 slices
Gateway request limits; tenant suspension propagation; immediate cross-service session revocation; tenant administration; durable audit delivery; complete OpenAPI; automated UI/security/integration coverage. Audit triggers block UPDATE, DELETE and TRUNCATE; least-privilege database grants still need setup. No milestone is marked complete.

See docs/IMPLEMENTATION_CHECKLIST.md and docs/FEATURE_IDEAS.md.

## Authentication setup
See [Local authentication runbook](docs/RUNBOOKS/LOCAL_AUTH.md) for keys, database variables and explicit synthetic-only provisioning. No hard-coded password is supplied. See ADR 0003 for session limitations.
