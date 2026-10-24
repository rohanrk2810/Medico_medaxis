package com.medicalerp.tenant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@RestController
@RequestMapping("/api/v1/tenants")
class TenantController {
 private final JdbcTemplate db; private final SessionAccess sessions;
 TenantController(JdbcTemplate db,SessionAccess sessions) { this.db=db; this.sessions=sessions; }
 record Tenant(UUID id,String code,String legalName,String country,String timezone,String currency,String status,long version) {}
 @GetMapping("/{id}") Tenant get(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) {
  TenantScope.require(jwt,id);
  sessions.requireActive(jwt);
  UUID branch;
  try {branch=UUID.fromString(jwt.getClaimAsString("branch_id"));}
  catch(RuntimeException e){throw new org.springframework.security.access.AccessDeniedException("Branch scope required");}
  if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM branches WHERE tenant_id=? AND id=?)",Boolean.class,id,branch)))
   throw new org.springframework.security.access.AccessDeniedException("Branch access denied");
  return db.query("SELECT id,code,legal_name,country,timezone,currency,status,version FROM tenants WHERE id=? AND status='ACTIVE'",
   (rs,n) -> new Tenant(rs.getObject("id",UUID.class),rs.getString("code"),rs.getString("legal_name"),rs.getString("country"),rs.getString("timezone"),rs.getString("currency"),rs.getString("status"),rs.getLong("version")),id)
   .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Tenant unavailable"));
 }
}
