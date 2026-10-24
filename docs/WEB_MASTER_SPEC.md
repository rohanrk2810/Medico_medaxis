# Medical ERP — Web master specification

Source: Medical_ERP_Web_Only_Angular_Codex_Specification.docx, version 1.0, 24 October 2026. Paragraph text extracted verbatim; table text follows document order.

MEDICAL ERP — WEB APPLICATIONAngular Frontend | Spring Boot Microservices | PostgreSQLCodex Implementation Specification — Web OnlyVersion 1.0 | 24 October 2026

1. Scope and strict boundaries
This document covers only the browser-based Medical ERP. Do not build a Windows EXE, Electron app, Android app or APK in this phase. The future clients may reuse the backend APIs after the web product is stable, but they are out of scope now.
Frontend must be Angular + TypeScript. Do not use React, Next.js or Vue.
Backend must use Java + Spring Boot and separately deployable microservices; do not replace this with a modular monolith.
PostgreSQL is the primary relational database. Each service owns its data; no cross-service database writes.
Build incrementally, run tests and report actual results. Do not claim production readiness just because the app compiles.
Do not use real patient data in development, screenshots, demo accounts or automated tests.
Layer
Required technology / rule
Frontend
Angular + TypeScript; Angular CLI; Angular Router; Reactive Forms; HttpClient; Angular Material.
Backend
Java + Spring Boot + Maven; Spring Security; Bean Validation; REST APIs; OpenAPI.
Database
PostgreSQL; Flyway or Liquibase migrations; separate service ownership.
Testing
Angular unit/component tests and E2E; Spring unit/integration/API tests; authorization tests.
Runtime
Docker for services; HTTPS reverse proxy; private database; backups, monitoring and CI/CD.

2. Product goal and user roles
Build a multi-tenant Medical ERP SaaS for small and medium private hospitals, nursing homes, clinics and diagnostic facilities. Each hospital is a tenant with its own users, branches, configuration, permissions and isolated data.
Role
Main permissions
Platform Super Admin
Provision tenants, platform configuration and service health; sensitive support access must be explicitly authorized and audited.
Hospital/Tenant Admin
Hospital profile, branches, departments, users, roles, modules and settings.
Reception
Patient registration/search, appointment scheduling, check-in and queue.
Doctor/Clinician
Authorized patient records, encounters, notes, diagnoses, orders and prescriptions.
Nurse
Authorized observations, nursing notes and care tasks.
Billing/Cashier
Invoices, receipts, payment status, approved refunds/voids and daily close.
Pharmacist
Medicine catalog, batches/expiry, dispensing, purchases, returns and stock ledger.
Lab Staff
Test catalog, orders, specimen status, results, verification and report release.
Inventory/Procurement
Suppliers, purchase orders, goods receipt, stock movements and adjustments.
Auditor/Manager
Read-only, permission-scoped reports and audit review.

3. Angular frontend requirements
3.1 Angular application structure
apps/web/  src/app/    core/      auth/ guards/ interceptors/ services/ layout/ error-handling/    shared/      components/ directives/ pipes/ validators/ models/    features/      dashboard/      tenant-admin/      users-roles/      patients/      appointments/      clinical/      admissions/      billing/      pharmacy/      inventory/      laboratory/      reports/      audit/    app.routes.ts    app.config.ts  src/environments/  public/  angular.json  package.json

Use standalone components and lazy-loaded feature routes where appropriate.
Use Reactive Forms for validated forms; use typed models and a central API service layer.
Use Angular HttpClient; add an interceptor for correlation IDs and authorized requests. Do not store secrets in frontend code.
Use route guards for navigation UX only. The backend must enforce all permissions.
Use a consistent layout: sidebar navigation, top bar with tenant/branch context, breadcrumbs, content area and user menu.
Use Angular Material components for forms, dialogs, tables, pagination, menus and accessible controls.
Use server-side pagination/filtering for large datasets. Do not load all patient records into browser memory.
Provide loading, empty, success, validation and error states. Never show success unless the API confirms it.
Make layout responsive for desktop and tablet; ensure keyboard navigation and visible focus states.
Tenant timezone, date format, currency, logo and theme must come from configuration, not hard-coded values.
3.2 Required screens
Feature area
Screens
Authentication
Login, forgot/reset password, optional MFA, session expired, access denied.
Platform admin
Tenant list, create/edit tenant, tenant status, enabled modules and audited support access workflow.
Hospital setup
Hospital profile, branches, departments, users, roles/permissions, numbering and locale settings.
Dashboard
Role-based KPI tiles, alerts, quick actions, date and branch filters.
Patients
Patient list/search, register/edit, patient profile, appointments, encounters and authorized documents.
Appointments
Calendar/list, create/reschedule/cancel, check-in and queue.
Clinical / OPD
Encounter list/detail, notes, vitals, diagnosis, orders and finalization.
IPD
Admission, bed board, transfer and discharge workflow.
Billing
Service catalog, invoice, payment records, receipt, refund/void approvals and daily close.
Pharmacy
Medicine items, batches, stock ledger, dispense/sale, purchase, returns, low-stock and expiry alerts.
Inventory
Suppliers, purchase orders, goods receipt, stock movements and adjustment approvals.
Laboratory
Test catalog, orders, specimen status, result entry, verification and reports.
Reports
Billing, outstanding balance, appointment, inventory, expiry and operational reports.
Security/audit
User sessions, access/audit event search and integration status.

4. Backend microservices (mandatory)
Each service must be a separate Spring Boot application/deployable unit with a clear owner and database/schema. A monorepo is acceptable for development; do not merge business domains into a single deployable application.
Service
Responsibility
api-gateway
External API routing, request limits and correlation IDs.
identity-service
Users, login, password reset, roles, permissions, sessions and refresh-token hashes.
tenant-service
Tenants, branches, module entitlements, configuration and subscription state.
patient-service
Patient demographics, identifiers, duplicate warnings and audited merge.
appointment-service
Appointments, check-in and queue.
clinical-service
Encounters, notes, diagnoses and clinical order references.
admission-service
Admissions, wards/beds, transfers and discharge.
billing-service
Invoices, receipts, balances, payment references and audited adjustments.
pharmacy-service
Medicine catalog, batch/expiry, dispensing and pharmacy stock ledger.
inventory-service
Suppliers, procurement, goods receipt and non-pharmacy stock.
lab-service
Tests, orders, specimen status, results and report release.
document-service
Document metadata; file bytes in private object storage.
notification-service
Email/SMS notification jobs and delivery status.
reporting-service
Read-optimized projections; not the source of truth.
audit-service
Append-only security and business audit events.
integration-service
External payment, lab, messaging and healthcare integrations.

Use REST/OpenAPI for synchronous requests. Use asynchronous messaging only where needed and document event schemas.
Every tenant-owned API request must validate tenant and branch scope server-side.
Do not perform cross-service database joins or writes. Use APIs, events or read projections.
Use idempotency for retryable billing/payment commands and a transactional outbox for critical events.
Start with a manageable service deployment setup but keep services independently buildable and deployable.
5. Core functional requirements
5.1 Tenant administration
Create tenant with unique tenant code, legal/business name, country, timezone, currency and contact.
Configure branches, departments, logo, invoice header/footer, numbering and enabled modules.
Ensure a tenant admin can only manage their own tenant.
Suspending a tenant must not silently delete medical or financial records.
5.2 Patients and appointments
Create/search patient records with tenant-scoped identifiers and duplicate warnings.
Use permission-controlled merge with audit history; do not silently merge records.
Create, reschedule, cancel and check in appointments; preserve status history.
Use server-side validation and collect only necessary information.
5.3 OPD/clinical
Create encounters linked to patient, provider, branch and timestamps.
Support configured notes, diagnoses, vitals and orders.
Track draft, signed/finalized and amended states; finalized records must not be silently overwritten.
Audit sensitive record access and changes.
5.4 IPD
Manage admissions, ward/bed assignment, transfer and discharge.
Prevent occupied-bed assignment except through an explicit audited override.
Track state changes, timestamps and responsible staff.
5.5 Billing/payments
Create invoices and receipts and daily cash-close reports.
Use decimal money types, explicit currency and rounding rules; never floating point for money.
Payment timeout means unknown status, not success. Store payment attempts separately from confirmed payments.
Refund/void requires authorization, reason and audit; do not delete posted financial records.
5.6 Pharmacy/inventory
Track SKU, batch/lot, expiry, supplier, purchase cost, sale price, tax config and stock ledger.
Use atomic transactions and stock validation; provide low-stock/near-expiry alerts.
Expired stock cannot be dispensed by default; exceptions must be explicitly authorized and audited where legally permissible.
Stock adjustments require reason, user, timestamp and approval policy.
5.7 Laboratory/reports
Manage tests, orders, specimen status, result entry, verification and report release.
Preserve corrections and amendments; separate result entry and approval where required.
Reports and exports must enforce the same permissions and tenant scope as the UI.
6. API design conventions
Prefix APIs with /api/v1 and manage breaking changes deliberately.
Use request/response DTOs, validation and OpenAPI documentation.
Use a consistent error response: timestamp, status, code, message, traceId and fieldErrors. Never return stack traces or secrets.
Use pagination and allowlisted sort/filter fields; avoid unbounded list endpoints.
Use UUIDs or another documented stable ID strategy.
Use migrations with Flyway/Liquibase; do not use automatic schema creation in production.
Use database constraints for tenant-scoped uniqueness and valid states.
Use optimistic locking/version fields for records that can be edited concurrently.
Domain
Example endpoints
Auth
POST /api/v1/auth/login ; POST /api/v1/auth/refresh ; POST /api/v1/auth/logout ; GET /api/v1/me
Tenants
POST /api/v1/tenants ; GET /api/v1/tenants/{tenantId} ; PATCH /api/v1/tenants/{tenantId}/settings
Patients
POST /api/v1/patients ; GET /api/v1/patients?query= ; GET /api/v1/patients/{id}
Appointments
POST /api/v1/appointments ; GET /api/v1/appointments ; PATCH /api/v1/appointments/{id}/status
Billing
POST /api/v1/invoices ; POST /api/v1/invoices/{id}/payments ; POST /api/v1/payments/{id}/refund
Pharmacy
POST /api/v1/pharmacy/items ; POST /api/v1/pharmacy/dispensations ; GET /api/v1/pharmacy/expiry-alerts

7. Security and privacy
Enforce role-based permissions plus tenant/branch scope on every backend request; deny by default.
Use HTTPS/TLS, least-privilege database/service credentials, protected backups and private object storage.
Use modern adaptive password hashing, short-lived credentials, revocation, login throttling and rate limits.
Do not log clinical notes, passwords, tokens, full payment-card data or unnecessary patient identifiers.
Use synthetic data for development, demos and tests.
Audit login attempts, role changes, sensitive record access, finalization, financial adjustments, exports and privileged support access.
Document retention, export, correction/deletion, breach response and incident handling.
Before selling in a jurisdiction, obtain qualified legal/security review of applicable health-data, privacy, hosting and transfer requirements. This specification is not a legal compliance certification.
8. Implementation phases and acceptance criteria
Milestone
Scope
Acceptance criteria
A0 — Repo audit
Inspect repository, stack, builds, tests and risks
Report current state and dependency-ordered plan; no code changes until approved.
A1 — Foundation
Angular setup, gateway, identity, tenant, migrations, audit
Build passes; login and cross-tenant authorization tests pass.
A2 — Hospital setup
Tenant profile, branches, departments, users, roles
Tenant admin only manages own tenant; validation tests pass.
A3 — Patients/appointments
Patient search/register, duplicate warning, scheduling/queue
Pagination, validation and authorization tests pass.
A4 — Billing/pharmacy
Invoices/receipts, stock ledger, batches/expiry
Atomic transactions, idempotency and audited adjustments tested.
A5 — Clinical/IPD/lab
Encounters, admissions/bed flow, lab orders/results
State transitions and access controls tested.
A6 — Hardening
Reports, deployment, monitoring, backup/restore test
Staging smoke/security checks pass and restore is demonstrated.

Do not mark a milestone complete just because code files exist. Run builds/tests and show results.
9. Testing and delivery
Test type
Minimum coverage
Angular tests
Components, forms/validators, services, route behavior and error states.
Backend unit tests
Business rules, permission checks, money/tax calculations and stock transitions.
Integration tests
Migrations, database transactions, API serialization and service adapters.
Authorization tests
Cross-tenant denial, role/branch scope, exports and sensitive endpoints.
E2E tests
Login, tenant setup, patient registration, appointment, invoice, pharmacy sale and reports.
Security checks
Dependency/secret scan, upload validation, rate limits and audit integrity.
Operations
Fresh deployment, health checks, backup/restore, migration and rollback rehearsal.

Separate local/dev, staging and production environments.
Use immutable container images, pinned dependencies, health/readiness endpoints and structured logs.
Automate backups and test restores periodically; agree recovery objectives with each hospital.
Store patient documents in private object storage with authorized short-lived access; never use public buckets for clinical files.
Document support hours, maintenance, patching, incident response and customer data export/termination.
10. Suggested repository layout
medical-erp/  docs/    WEB_MASTER_SPEC.md    ARCHITECTURE.md    ADR/    API/    SECURITY/    RUNBOOKS/  apps/    web-angular/  services/    api-gateway/    identity-service/    tenant-service/    patient-service/    appointment-service/    clinical-service/    admission-service/    billing-service/    pharmacy-service/    inventory-service/    lab-service/    document-service/    notification-service/    reporting-service/    audit-service/    integration-service/  contracts/    openapi/    events/  platform/    docker/    deployment/    monitoring/  scripts/  .github/workflows/  README.md

If the repository already exists, map existing files safely instead of blindly moving or replacing them.
11. Codex master prompt — first action only
You are the senior architect and implementation engineer for my Medical ERP WEB APPLICATION.Read the full WEB-ONLY specification before making changes.STRICT SCOPE:- Frontend: Angular + TypeScript. Do NOT use React, Next.js or Vue.- Backend: Java + Spring Boot, separate deployable microservices.- Database: PostgreSQL with migrations and clear service ownership.- Build ONLY the WEB APPLICATION in this phase.- Do NOT build Windows EXE, Electron, Android or APK now.FIRST ACTION — REPOSITORY RECONNAISSANCE ONLY:1. Inspect the existing repository, current stack, working features, tests and build commands.2. Preserve working code. Do not perform large refactors or destructive changes.3. Create/update docs/WEB_MASTER_SPEC.md, docs/ARCHITECTURE.md, an implementation checklist and architecture decision records.4. Report current architecture, feature gaps, test/build status, risks and a dependency-ordered Phase 1 plan.5. Do not implement features until I approve the plan.ARCHITECTURE RULES:Use separate deployable microservices with explicit data ownership. No cross-service database writes. Use versioned REST/OpenAPI, server-side tenant/branch authorization, migrations, audit logs, tests, structured logs and idempotency where appropriate.WORKING METHOD AFTER APPROVAL:Implement small vertical slices. Each slice must include Angular UI, API, validation, authorization, migration if needed and tests. Run formatter/linter/tests/build, fix failures, update documentation and report exact commands and results.Never claim a command passed unless it was run. Do not disable security/tests to hide failures. Never use real patient data or commit secrets. Ask before destructive changes, production deployment, paid services or irreversible migrations.Return now:A) current architecture and stack;B) existing features and test/build status;C) gap analysis against this specification;D) proposed web-only implementation plan in dependency order;E) decisions needed from me.Do not code until I approve the plan.

12. Codex implementation prompt after plan approval
Implement the approved Medical ERP WEB phase only, using Angular + TypeScript frontend, Java Spring Boot microservices and PostgreSQL.Follow WEB_MASTER_SPEC.md. Work one milestone at a time:1. build/test baseline and local environment;2. gateway, identity, tenant isolation, migrations and audit;3. hospital setup and roles;4. patients and appointments;5. billing and pharmacy/inventory MVP;6. clinical, IPD and lab;7. reports, staging deployment, backup/restore and security hardening.For every slice implement UI, API, validation, authorization and tests together. Run checks and report exact outcomes. Stop at each milestone for review. Do not build Windows or Android apps.

13. Definition of done
Feature works in Angular UI and performs real backend/database operations.
Authorization tests include denied access and cross-tenant attempts.
Relevant tests and builds pass; failures are reported honestly.
Migrations, API docs, README and operational instructions are updated.
No real patient data or secrets in source, logs or test fixtures.
Known limitations and acceptance criteria are explicitly reported.
14. Official references
Angular documentation: https://angular.dev/
Angular Material: https://material.angular.dev/
Spring Boot requirements: https://docs.spring.io/spring-boot/system-requirements.html
Spring Security: https://docs.spring.io/spring-security/reference/
PostgreSQL documentation: https://www.postgresql.org/docs/