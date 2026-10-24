CREATE TABLE user_grants (
 user_id uuid NOT NULL REFERENCES users(id), branch_id uuid NOT NULL,
 permission varchar(100) NOT NULL, PRIMARY KEY(user_id,branch_id,permission)
);
CREATE TABLE login_limits (
 key_hash varchar(64) PRIMARY KEY, failures integer NOT NULL DEFAULT 0,
 window_start timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE identity_outbox (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, actor_id uuid,
 action varchar(80) NOT NULL, occurred_at timestamptz NOT NULL DEFAULT now(),
 delivered_at timestamptz
);
CREATE INDEX refresh_user_idx ON refresh_sessions(user_id);
