# Proposed architecture

Status: proposed; implementation awaits A0 plan approval.

## Scope
Browser application only: Angular, TypeScript, standalone components, lazy routes, Reactive Forms, HttpClient and Angular Material. No desktop or mobile client.

## Request path
Browser → HTTPS reverse proxy → API gateway → independently deployable Spring Boot services → service-owned PostgreSQL databases. Files use private object storage through document-service. The browser never connects to databases.

## Service ownership
| Service | Owned data / responsibility |
| --- | --- |
| api-gateway | Routing, correlation IDs, request limits; no clinical database |
| identity-service | Users, password hashes, roles, permissions, sessions, refresh-token hashes |
| tenant-service | Tenants, branches, departments, settings, module entitlements |
| patient-service | Demographics, tenant identifiers, duplicate/merge history |
| appointment-service | Scheduling, queue, status history |
| clinical-service | Encounters, notes, diagnoses, order references |
| admission-service | Beds, admissions, transfers, discharge |
| billing-service | Invoices, payment attempts, confirmed receipts, adjustments, outbox |
| pharmacy-service | Medicines, batches, dispensations, pharmacy stock ledger |
| inventory-service | Suppliers, procurement, receipts, non-pharmacy stock |
| lab-service | Tests, specimens, results, verification, amendments |
| document-service | Metadata and private object references |
| notification-service | Delivery jobs and status |
| reporting-service | Authorized read projections |
| audit-service | Append-only security and business events |
| integration-service | External provider adapters and integration status |

## Security and consistency
Each service verifies credential validity, permission, tenant, branch and record scope independently. Deny by default. Client-supplied tenant headers cannot grant scope. Platform support access requires explicit scoped authorization and audit. Tenant suspension preserves records and denies ordinary operational access.

Use versioned REST DTOs, bounded pagination, allowlisted filters, consistent sanitized errors and OpenAPI. UUID identifiers and optimistic versions identify editable records. Flyway owns schema changes; application roles cannot perform DDL or write another service database. Foreign references across services are IDs validated through APIs, without cross-database joins.

Use decimal monetary values with explicit currency and rounding; immutable posted financial history. Retryable financial commands require tenant-scoped idempotency and atomic outbox persistence. Audit delivery must be durable and deduplicated. Document event contracts before adding messaging infrastructure. Finalized clinical records and released lab results require amendments rather than overwrite.

## Delivery
Monorepo: apps/web-angular, services/<service>, contracts/{openapi,events}, platform/{docker,deployment,monitoring}, scripts, docs, .github/workflows. Each service has its own build, migration location, container and database credentials. Begin local deployment with foundation services; introduce other services at their milestones.

Use synthetic fixtures only. Separate development, staging and production. Pin compatible dependency versions during A1, commit lockfiles and Maven wrapper; configure secrets externally. Add health checks, structured redacted logs, protected backups and restore instructions. Production readiness requires measured acceptance evidence and jurisdiction-specific legal/security review.
