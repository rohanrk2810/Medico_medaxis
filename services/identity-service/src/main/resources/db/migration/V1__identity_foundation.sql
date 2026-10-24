CREATE TABLE users (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, email varchar(254) NOT NULL,
 password_hash varchar(255) NOT NULL, enabled boolean NOT NULL DEFAULT true,
 version bigint NOT NULL DEFAULT 0, UNIQUE(tenant_id,email)
);
CREATE TABLE refresh_sessions (
 id uuid PRIMARY KEY, user_id uuid NOT NULL REFERENCES users(id),
 token_hash varchar(128) NOT NULL UNIQUE, expires_at timestamptz NOT NULL,
 revoked_at timestamptz, created_at timestamptz NOT NULL DEFAULT now()
);
