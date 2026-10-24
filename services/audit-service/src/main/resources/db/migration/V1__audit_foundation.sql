CREATE TABLE audit_events (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, branch_id uuid,
 actor_id uuid NOT NULL, action varchar(100) NOT NULL,
 resource_type varchar(100) NOT NULL, resource_id uuid,
 trace_id varchar(100) NOT NULL, occurred_at timestamptz NOT NULL,
 received_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX audit_tenant_time ON audit_events(tenant_id,occurred_at DESC);
CREATE FUNCTION reject_audit_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Audit events are append-only'; END; $$;
CREATE TRIGGER immutable_audit BEFORE UPDATE OR DELETE ON audit_events
FOR EACH ROW EXECUTE FUNCTION reject_audit_mutation();
