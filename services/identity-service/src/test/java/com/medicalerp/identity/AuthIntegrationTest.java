package com.medicalerp.identity;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17.6-alpine");
 @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) throws Exception {
  KeyPairGenerator gen=KeyPairGenerator.getInstance("RSA");gen.initialize(2048);KeyPair pair=gen.generateKeyPair();
  Path privateFile=Files.createTempFile("test-identity-private-",".pem"),publicFile=Files.createTempFile("test-identity-public-",".pem");
  Files.writeString(privateFile,Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));Files.writeString(publicFile,Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));privateFile.toFile().deleteOnExit();publicFile.toFile().deleteOnExit();
  registry.add("spring.datasource.url",postgres::getJdbcUrl);registry.add("spring.datasource.username",postgres::getUsername);registry.add("spring.datasource.password",postgres::getPassword);
  registry.add("auth.private-key-file",privateFile::toString);registry.add("auth.public-key-file",publicFile::toString);registry.add("auth.issuer",()->"http://identity.test");
 }
 @Autowired TestRestTemplate http;@Autowired JdbcTemplate db;@Autowired PasswordEncoder encoder;
 @Test void loginRotationLogoutAndScopeDenials(){
  UUID tenant=UUID.randomUUID(),branch=UUID.randomUUID(),user=UUID.randomUUID();
  db.update("INSERT INTO users(id,tenant_id,email,password_hash) VALUES (?,?,?,?)",user,tenant,"synthetic@example.test",encoder.encode("Synthetic-Only-42!"));
  db.update("INSERT INTO user_grants(user_id,branch_id,permission) VALUES (?,?,?)",user,branch,"tenant:read");
  Map<String,Object> login=new HashMap<>(Map.of("tenantId",tenant,"branchId",branch,"email","synthetic@example.test","password","wrong"));
  assertEquals(401,http.postForEntity("/api/v1/auth/login",login,Map.class).getStatusCode().value());
  login.put("password","Synthetic-Only-42!");login.put("branchId",UUID.randomUUID());
  assertEquals(401,http.postForEntity("/api/v1/auth/login",login,Map.class).getStatusCode().value());
  login.put("branchId",branch);login.put("tenantId",UUID.randomUUID());
  assertEquals(401,http.postForEntity("/api/v1/auth/login",login,Map.class).getStatusCode().value());
  login.put("tenantId",tenant);
  var response=http.postForEntity("/api/v1/auth/login",login,Map.class);assertEquals(200,response.getStatusCode().value());
  Map<?,?> tokens=response.getBody();assertNotNull(tokens);String refresh=(String)tokens.get("refreshToken");
  HttpHeaders headers=new HttpHeaders();headers.setBearerAuth((String)tokens.get("accessToken"));
  assertEquals(200,http.exchange("/api/v1/me",HttpMethod.GET,new HttpEntity<>(headers),Map.class).getStatusCode().value());
  var renewed=http.postForEntity("/api/v1/auth/refresh",Map.of("refreshToken",refresh,"branchId",branch),Map.class);assertEquals(200,renewed.getStatusCode().value());
  assertEquals(401,http.postForEntity("/api/v1/auth/refresh",Map.of("refreshToken",refresh,"branchId",branch),Map.class).getStatusCode().value());
  assertEquals(401,http.exchange("/api/v1/me",HttpMethod.GET,new HttpEntity<>(headers),Map.class).getStatusCode().value());
  assertEquals(204,http.postForEntity("/api/v1/auth/logout",Map.of("refreshToken",renewed.getBody().get("refreshToken")),Void.class).getStatusCode().value());
  assertEquals(401,http.postForEntity("/api/v1/auth/refresh",Map.of("refreshToken",renewed.getBody().get("refreshToken"),"branchId",branch),Map.class).getStatusCode().value());
  assertTrue(db.queryForObject("SELECT count(*) FROM identity_outbox",Integer.class)>=6);
  assertEquals(64,db.queryForObject("SELECT length(token_hash) FROM refresh_sessions LIMIT 1",Integer.class));
 }
 @Test void throttlesRepeatedInvalidLogins(){
  var request=Map.of("tenantId",UUID.randomUUID(),"branchId",UUID.randomUUID(),"email","blocked@example.test","password","invalid");
  for(int i=0;i<6;i++)assertEquals(401,http.postForEntity("/api/v1/auth/login",request,Map.class).getStatusCode().value());
  assertTrue(db.queryForObject("SELECT count(*) FROM identity_outbox WHERE action='LOGIN_THROTTLED'",Integer.class)>0);
 }
}
