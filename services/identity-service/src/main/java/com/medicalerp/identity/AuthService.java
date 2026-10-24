package com.medicalerp.identity;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
class AuthService {
 private final JdbcTemplate db; private final PasswordEncoder passwords;private final JwtEncoder encoder;private final String issuer;private final String dummyHash;
 AuthService(JdbcTemplate db,PasswordEncoder passwords,JwtEncoder encoder,@Value("${auth.issuer}") String issuer){this.db=db;this.passwords=passwords;this.encoder=encoder;this.issuer=issuer;this.dummyHash=passwords.encode(UUID.randomUUID().toString());}
 record Tokens(String accessToken,String refreshToken,long expiresIn,UUID tenantId,UUID branchId){}
 record User(UUID id,UUID tenantId,String passwordHash){}
 record Session(UUID id,UUID userId,Instant expires,boolean revoked){}
 static String hash(String raw){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private void audit(UUID tenant,UUID actor,String action){db.update("INSERT INTO identity_outbox(id,tenant_id,actor_id,action) VALUES (?,?,?,?)",UUID.randomUUID(),tenant,actor,action);}
 @Transactional
 public Tokens login(UUID tenant,UUID branch,String email,String password){
  String key=hash(tenant+":"+email.toLowerCase(Locale.ROOT));
  db.update("INSERT INTO login_limits(key_hash) VALUES (?) ON CONFLICT DO NOTHING",key);
  var limit=db.queryForMap("SELECT failures,window_start FROM login_limits WHERE key_hash=? FOR UPDATE",key);
  boolean expired=((java.sql.Timestamp)limit.get("window_start")).toInstant().isBefore(Instant.now().minusSeconds(900));
  if(expired)db.update("UPDATE login_limits SET failures=0,window_start=now() WHERE key_hash=?",key);
  else if(((Number)limit.get("failures")).intValue()>=5){audit(tenant,null,"LOGIN_THROTTLED");return null;}
  var users=db.query("SELECT id,tenant_id,password_hash FROM users WHERE tenant_id=? AND lower(email)=? AND enabled=true",(rs,n)->new User(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),rs.getString(3)),tenant,email.toLowerCase(Locale.ROOT));
  User user=users.isEmpty()?null:users.getFirst();
  boolean valid=password.getBytes(StandardCharsets.UTF_8).length<=72 && passwords.matches(password,user==null?dummyHash:user.passwordHash());
  if(!valid||user==null||permissions(user.id(),branch).isEmpty()){
   db.update("UPDATE login_limits SET failures=failures+1 WHERE key_hash=?",key);audit(tenant,user==null?null:user.id(),"LOGIN_DENIED");return null;
  }
  db.update("UPDATE login_limits SET failures=0 WHERE key_hash=?",key);
  audit(tenant,user.id(),"LOGIN_SUCCEEDED");return issue(user,branch);
 }
 private List<String> permissions(UUID user,UUID branch){return db.queryForList("SELECT permission FROM user_grants WHERE user_id=? AND branch_id=? ORDER BY permission",String.class,user,branch);}
 private Tokens issue(User user,UUID branch){
  byte[] bytes=new byte[48];new SecureRandom().nextBytes(bytes);String refresh=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  UUID sid=UUID.randomUUID();Instant now=Instant.now();
  db.update("INSERT INTO refresh_sessions(id,user_id,token_hash,expires_at) VALUES (?,?,?,?)",sid,user.id(),hash(refresh),java.sql.Timestamp.from(now.plusSeconds(28800)));
  var claims=JwtClaimsSet.builder().issuer(issuer).subject(user.id().toString()).audience(List.of("medical-erp")).issuedAt(now).expiresAt(now.plusSeconds(300)).id(UUID.randomUUID().toString())
   .claim("tenant_id",user.tenantId().toString()).claim("branch_id",branch.toString()).claim("sid",sid.toString()).claim("scope",String.join(" ",permissions(user.id(),branch))).build();
  String access=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).keyId("identity-v1").build(),claims)).getTokenValue();
  return new Tokens(access,refresh,300,user.tenantId(),branch);
 }
 @Transactional
 public Tokens refresh(String raw,UUID branch){
  var sessions=db.query("SELECT id,user_id,expires_at,revoked_at IS NOT NULL FROM refresh_sessions WHERE token_hash=? FOR UPDATE",(rs,n)->new Session(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),rs.getTimestamp(3).toInstant(),rs.getBoolean(4)),hash(raw));
  if(sessions.isEmpty())return null;Session session=sessions.getFirst();
  if(session.revoked()||!session.expires().isAfter(Instant.now()))return null;
  var users=db.query("SELECT id,tenant_id,password_hash FROM users WHERE id=? AND enabled=true",(rs,n)->new User(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),rs.getString(3)),session.userId());
  if(users.isEmpty()||permissions(session.userId(),branch).isEmpty())return null;
  db.update("UPDATE refresh_sessions SET revoked_at=now() WHERE id=?",session.id());
  audit(users.getFirst().tenantId(),session.userId(),"SESSION_REFRESHED");return issue(users.getFirst(),branch);
 }
 @Transactional
 public void logout(String raw){
  var users=db.query("SELECT u.id,u.tenant_id,u.password_hash FROM users u JOIN refresh_sessions s ON s.user_id=u.id WHERE s.token_hash=?",(rs,n)->new User(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),rs.getString(3)),hash(raw));
  db.update("UPDATE refresh_sessions SET revoked_at=now() WHERE token_hash=? AND revoked_at IS NULL",hash(raw));
  if(!users.isEmpty())audit(users.getFirst().tenantId(),users.getFirst().id(),"LOGOUT");
 }
 boolean active(String sid,String user,String tenant,String branch,String scope){
  try {
   UUID userId=UUID.fromString(user),branchId=UUID.fromString(branch);
   boolean live=Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM refresh_sessions s JOIN users u ON u.id=s.user_id WHERE s.id=? AND u.id=? AND u.tenant_id=? AND u.enabled=true AND s.revoked_at IS NULL AND s.expires_at>now())",Boolean.class,UUID.fromString(sid),userId,UUID.fromString(tenant)));
   var granted=permissions(userId,branchId);
   return live && scope!=null && !scope.isBlank() && !granted.isEmpty() && granted.containsAll(List.of(scope.split(" ")));
  }catch(IllegalArgumentException e){return false;}
 }
}
