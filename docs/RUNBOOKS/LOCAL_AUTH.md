# Local authentication slice

Requires JDK 21, Node 24, Docker for integration tests and PostgreSQL 17 for running services. Use `./mvnw verify`, then `npm ci && npm run build` inside apps/web-angular. Dependencies require network access on first run.

## Keys
Generate local keys outside the repository, with directory mode 0700:

```sh
mkdir -m 700 /tmp/medical-local-keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out /tmp/medical-local-keys/private.pem
openssl pkey -in /tmp/medical-local-keys/private.pem -pubout -out /tmp/medical-local-keys/public.pem
chmod 600 /tmp/medical-local-keys/private.pem
```

Never commit these files. Configure `IDENTITY_PRIVATE_KEY_FILE` and `IDENTITY_PUBLIC_KEY_FILE` with their absolute paths. Production needs managed keys and rotation.

## Service configuration
Each service uses its own PostgreSQL database and credentials: `DATABASE_URL` (JDBC URL), `DATABASE_USER`, `DATABASE_PASSWORD`. Flyway runs on startup. Do not point services at each other's databases.

For identity set `IDENTITY_ISSUER_URI=http://localhost:8081` and the key-file variables. For tenant use the same issuer plus `IDENTITY_JWKS_URI=http://localhost:8081/api/v1/auth/jwks`. Gateway defaults route to identity 8081 and tenant 8082; it listens on 8080. Run each built jar from services/<name>/target. Start Angular with `npm start`; its development proxy sends /api to the gateway.

## Synthetic account
Only for local development, enable `SPRING_PROFILES_ACTIVE=synthetic-dev` on identity and tenant. Set the same randomly generated `DEV_TENANT_ID` and `DEV_BRANCH_ID` UUIDs for both. Identity also needs a random `DEV_USER_ID` and a private `DEV_PASSWORD` (16+ characters, max 72 UTF-8 bytes). The synthetic email is admin@example.test. No fixed password is shipped. Sign in using these hospital/branch IDs and credentials. Seed inserts never replace existing user passwords.

Synthetic locale IN/Asia/Kolkata/INR is example fixture data only, not a product or jurisdiction default. The dashboard reads locale values from tenant-service.

## Current limitations
Tenant admin provisioning screens are pending. Refresh is manual and memory-only sessions end on reload. Resource-service JWT revocation is bounded by five-minute expiry. Tenant suspension propagation, audit outbox delivery, password reset, MFA, global request limits and full browser E2E remain pending. Do not deploy this development slice as a finished ERP.
