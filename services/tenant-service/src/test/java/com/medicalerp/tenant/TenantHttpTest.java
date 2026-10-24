package com.medicalerp.tenant;
import java.net.*;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class TenantHttpTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17.6-alpine");
 static HttpServer identity;static AtomicInteger identityStatus=new AtomicInteger(200);
 @DynamicPropertySource static void configure(DynamicPropertyRegistry r)throws Exception{
  identity=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  identity.createContext("/me",exchange->{int code=identityStatus.get();if(!"Bearer synthetic-token".equals(exchange.getRequestHeaders().getFirst("Authorization")))code=401;exchange.sendResponseHeaders(code,-1);exchange.close();});identity.start();
  r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);
  r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",()->"http://identity.test");
  r.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",()->"http://identity.test/jwks");
  r.add("identity.session-url",()->"http://127.0.0.1:"+identity.getAddress().getPort()+"/me");
 }
 @AfterAll static void stop(){identity.stop(0);}
 @Autowired TestRestTemplate http;@Autowired JdbcTemplate db;@MockitoBean JwtDecoder decoder;
 UUID tenant,branch;
 @BeforeEach void seed(){tenant=UUID.randomUUID();branch=UUID.randomUUID();identityStatus.set(200);
  db.update("INSERT INTO tenants(id,code,legal_name,country,timezone,currency,status) VALUES (?,?, 'Synthetic','IN','Asia/Kolkata','INR','ACTIVE')",tenant,tenant.toString());
  db.update("INSERT INTO branches(id,tenant_id,code,name) VALUES (?,?,'TEST','Synthetic')",branch,tenant);
 }
 void token(UUID tenantClaim,UUID branchClaim,String scope){when(decoder.decode("synthetic-token")).thenReturn(Jwt.withTokenValue("synthetic-token").header("alg","RS256").subject(UUID.randomUUID().toString()).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).claim("tenant_id",tenantClaim.toString()).claim("branch_id",branchClaim.toString()).claim("scope",scope).build());}
 int get(UUID requested){HttpHeaders headers=new HttpHeaders();headers.setBearerAuth("synthetic-token");return http.exchange("/api/v1/tenants/"+requested,HttpMethod.GET,new HttpEntity<>(headers),String.class).getStatusCode().value();}
 @Test void ownTenantAndBranchAllowed(){token(tenant,branch,"tenant:read");assertEquals(200,get(tenant));}
 @Test void crossTenantDenied(){token(UUID.randomUUID(),branch,"tenant:read");assertEquals(403,get(tenant));}
 @Test void foreignBranchDenied(){token(tenant,UUID.randomUUID(),"tenant:read");assertEquals(403,get(tenant));}
 @Test void missingPermissionDenied(){token(tenant,branch,"patient:read");assertEquals(403,get(tenant));}
 @Test void revokedSessionDenied(){token(tenant,branch,"tenant:read");identityStatus.set(401);assertEquals(401,get(tenant));}
 @Test void identityFailureDeniesAccess(){token(tenant,branch,"tenant:read");identityStatus.set(503);assertEquals(503,get(tenant));}
 @Test void unexpectedIdentityResponseDenied(){token(tenant,branch,"tenant:read");identityStatus.set(302);assertEquals(503,get(tenant));}
 @Test void suspendedTenantUnavailable(){token(tenant,branch,"tenant:read");db.update("UPDATE tenants SET status='SUSPENDED' WHERE id=?",tenant);assertEquals(404,get(tenant));}
 @Test void anonymousDenied(){assertEquals(401,http.getForEntity("/api/v1/tenants/"+tenant,String.class).getStatusCode().value());}
}
