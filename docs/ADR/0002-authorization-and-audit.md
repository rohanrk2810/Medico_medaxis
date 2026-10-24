# ADR 0002: Server-enforced scope and durable audit

Status: proposed pending A0 approval.

Decision: deny by default at each service, with validated identity, permissions, tenant and branch scope. Route guards serve navigation only. Use short-lived credentials and revocable refresh sessions; exact signing/key rotation and session transport configuration are A1 design work. Use durable transactional audit/outbox records for sensitive changes and an append-only audit owner; deduplicate delivery.

Consequence: service-to-service access and support access need explicit scoped policies. Logout/revocation behavior, cookie CSRF protections and cross-tenant negative tests must be established before accepting A1.
