# Dependency-ordered implementation checklist

All milestones remain incomplete. Documentation does not establish working functionality.

- [x] A0: Read supplied specification and inspect available workspace/tools.
- [x] A0: Record architecture, gaps, risks and proposed decisions.
- [x] A0: User approved implementation and continued routine work on 24 October 2026.
- [ ] A1: Initialize repository, Angular CLI/Material application and independently buildable gateway, identity, tenant and audit services; pin dependencies and add Maven wrapper.
- [ ] A1: Add local PostgreSQL ownership, Flyway migrations, development configuration and secret templates.
- [ ] A1: Implement login/session refresh/logout, permission and tenant/branch checks, tenant configuration, audit/outbox foundation and Angular authenticated layout.
- [ ] A1: Add UI/API contracts and tests; run builds, authorization tests (including cross-tenant and branch denial), login E2E and migration integration tests. Review milestone.
- [ ] A2: Hospital profile, branches/departments, users/roles, entitlements and locale configuration; verify administration scope. Review milestone.
- [ ] A3: Patient registration/search/pagination, duplicate warnings and audited merge; appointments, status history and queue. Verify UI/API/authorization. Review milestone.
- [ ] A4: Decimal invoices/receipts, idempotent payments, unknown timeout status, approved adjustments, pharmacy batches/expiry/atomic ledger; procurement/inventory workflows. Verify concurrency and retries. Review milestone.
- [ ] A5: Clinical draft/sign/amend, admissions/bed concurrency/transfer/discharge, lab entry/verification/release/amendments; private documents. Verify state transitions and authorization. Review milestone.
- [ ] A6: Authorized reporting projections/exports, notification/integration adapters, monitoring, deployment and rollback runbooks, dependency/secret scans and backup/restore rehearsal. Review milestone.

Each slice includes Angular UI, real APIs, DTO validation, server authorization, migrations, audit, relevant tests and documentation. Never mark a check complete without evidence. User requested full hands-on testing later on laptop; mark all deferred checks explicitly and never call untested milestones complete.
