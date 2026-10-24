package com.medicalerp.tenant;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
final class TenantScope {
 static UUID tenant(Jwt jwt) {
  try { return UUID.fromString(jwt.getClaimAsString("tenant_id")); }
  catch (RuntimeException e) { throw new AccessDeniedException("Tenant scope required"); }
 }
 static void require(Jwt jwt, UUID requested) {
  if (!tenant(jwt).equals(requested)) throw new AccessDeniedException("Tenant access denied");
 }
}
