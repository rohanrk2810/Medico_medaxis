package com.medicalerp.tenant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.junit.jupiter.api.Assertions.*;
class TenantScopeTest {
 private Jwt token(String tenant) {return Jwt.withTokenValue("synthetic").header("alg","RS256").claim("tenant_id",tenant).build();}
 @Test void allowsOwnTenant() {UUID id=UUID.randomUUID();assertDoesNotThrow(() -> TenantScope.require(token(id.toString()),id));}
 @Test void deniesOtherTenant() {assertThrows(AccessDeniedException.class,() -> TenantScope.require(token(UUID.randomUUID().toString()),UUID.randomUUID()));}
 @Test void deniesMalformedScope() {assertThrows(AccessDeniedException.class,() -> TenantScope.tenant(token("invalid")));}
 @Test void deniesMissingScope() {Jwt jwt=Jwt.withTokenValue("synthetic").header("alg","RS256").claim("sub","user").build();assertThrows(AccessDeniedException.class,() -> TenantScope.tenant(jwt));}
}
