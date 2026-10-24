# A0 repository audit — 24 October 2026

## Current architecture and features
The supplied workspace contains no existing application, package manifest, Maven build, migrations, tests or project instructions. The .git directory is an empty placeholder; `git -C /workspace status --short` fails: not a git repository. This is a greenfield project. No existing working code was replaced. Proposed project root: /workspace/medical-erp.

## Actual tool checks
| Command/check | Observed result |
| --- | --- |
| Workspace listing and file discovery | No application source or AGENTS.md found |
| node --version | v24.19.0 |
| npm --version | 11.9.0 |
| java -version | OpenJDK 21.0.12.1 |
| mvn --version | Failed: command not found |
| command -v docker | /usr/local/bin/docker; daemon readiness not checked |
| command -v psql / ng | Not found |
| Application build, unit/integration/E2E tests | Not run: no implementation or build definitions exist |

Managed environment status reports running, connected, enforced unrestricted network policy and no configured secrets or outbound identities. No dependency installation, Docker startup or external provisioning was attempted.

## Gaps and risks
Every functional area in the specification is unimplemented. Foundation risks are authorization isolation, durable audit, credential/session revocation and per-service migrations. Later high-risk rules are stock/bed concurrency, financial idempotency and clinical/lab finalization. Infrastructure, object storage, email/SMS/payment providers, backup recovery objectives and jurisdiction requirements remain unconfigured. Maven must be bootstrapped with a pinned wrapper; Docker engine availability must be checked before integration tests.

## Concrete Phase 1 proposal
1. Bootstrap Angular CLI/Material and Java 21 Spring Boot Maven monorepo with compatible pinned versions, wrapper, lint/build scripts and CI definitions.
2. Create separate gateway, identity, tenant and audit applications, individual database credentials and Flyway migrations; synthetic development seeds only.
3. Build tenant-aware login, short-lived credentials, hashed rotating refresh sessions, logout/revocation and audited failed/successful authentication. Enforce permissions/tenant/branch scope at services.
4. Build Angular login, protected layout, access-denied/session-expired handling, correlation interceptor, typed API services and tenant-provided locale configuration.
5. Implement durable audit contracts/delivery and foundation health endpoints. Document local startup and required environment variables.
6. Verify build, login, revocation, cross-tenant/branch denials, migrations and login E2E; record exact results and stop for A1 review. Any laptop-deferred test stays pending.

## Proposed defaults and decisions
Proposed defaults: Java 21, compatible supported Angular/Spring releases chosen at implementation, Maven wrapper, Flyway, UUIDs, service-owned PostgreSQL databases, same-origin gateway and secure cookie-based refresh session. No paid services or production deployment in A1.

Required now: approval to implement the A1 plan, as requested in sections 8 and 11 of the source specification. Optional before setup: product name and initial hospital locale/timezone/currency. Optional configuration remains tenant-owned; do not infer jurisdiction from conversation language. Hosting, provider integrations, retention and recovery objectives can be decided before their dependent milestones.
