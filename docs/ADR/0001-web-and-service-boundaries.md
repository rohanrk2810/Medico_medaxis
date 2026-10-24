# ADR 0001: Web-only client and separate service ownership

Status: proposed pending A0 approval; technology constraints mandated by supplied specification.

Decision: use Angular/TypeScript/Material for the browser, Java/Spring Boot independently deployable services and PostgreSQL databases with Flyway. Keep services in one repository with separate builds and credentials. Use APIs/events across boundaries; prohibit cross-service database writes and joins.

Consequence: more local orchestration and contract testing than a single application. Stage deployments by milestone rather than creating nominal empty services and claiming completion. Future desktop/mobile clients are outside this phase.
