# ADR 0003: Initial browser session transport

Status: implemented for the first authentication slice.

The browser keeps access and refresh tokens in memory only. It explicitly submits the bearer access token and refresh body over same-origin APIs. No tokens are persisted to localStorage, sessionStorage or cookies. Reload requires signing in again. This avoids ambient cookie authentication; CSRF protection must be revisited before introducing cookie sessions.

Access tokens expire after five minutes. Refresh tokens contain 48 random bytes; only SHA-256 hashes are persisted. Refresh rotation is serialized through a database row lock. Revoked/expired refresh tokens are rejected. Logout revokes the submitted refresh session. Identity /me checks session revocation and current branch grants immediately. Tenant-service verifies every configuration read against /me with bounded timeouts, denying access if identity is unavailable. Future services must adopt equivalent enforcement. Reuse-family detection, key rotation and absolute session lifetime remain outstanding hardening work.

Only pre-provisioned users with branch-specific grants can sign in. Tenant status must still be enforced consistently across services before A1 acceptance. Identity audit events are transactionally recorded in the outbox; delivery to audit-service is pending.
