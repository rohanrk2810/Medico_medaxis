CREATE TABLE tenants (
 id uuid PRIMARY KEY, code varchar(40) NOT NULL UNIQUE,
 legal_name varchar(200) NOT NULL, country char(2) NOT NULL,
 timezone varchar(80) NOT NULL, currency char(3) NOT NULL,
 status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','SUSPENDED')),
 version bigint NOT NULL DEFAULT 0, created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE branches (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL REFERENCES tenants(id),
 code varchar(40) NOT NULL, name varchar(160) NOT NULL,
 version bigint NOT NULL DEFAULT 0, UNIQUE(tenant_id,code), UNIQUE(tenant_id,id)
);
