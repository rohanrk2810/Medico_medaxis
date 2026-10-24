CREATE UNIQUE INDEX users_tenant_email_normalized ON users(tenant_id,lower(email));
