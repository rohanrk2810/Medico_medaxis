package com.medicalerp.identity;
import java.util.*;
import com.nimbusds.jose.jwk.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController
class AuthController {
 private final AuthService auth;private final RSAKey key;
 AuthController(AuthService auth,RSAKey key){this.auth=auth;this.key=key;}
 record Login(@NotNull UUID tenantId,@NotNull UUID branchId,@NotBlank @Email @Size(max=254) String email,@NotBlank @Size(max=72) String password){}
 record Refresh(@NotBlank @Size(max=128) String refreshToken,@NotNull UUID branchId){}
 record Logout(@NotBlank @Size(max=128) String refreshToken){}
 @PostMapping("/api/v1/auth/login") ResponseEntity<?> login(@Valid @RequestBody Login request){return response(auth.login(request.tenantId(),request.branchId(),request.email(),request.password()));}
 @PostMapping("/api/v1/auth/refresh") ResponseEntity<?> refresh(@Valid @RequestBody Refresh request){return response(auth.refresh(request.refreshToken(),request.branchId()));}
 @PostMapping("/api/v1/auth/logout") ResponseEntity<?> logout(@Valid @RequestBody Logout request){auth.logout(request.refreshToken());return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();}
 @GetMapping("/api/v1/auth/jwks") Map<String,Object> keys(){return new JWKSet(key.toPublicJWK()).toJSONObject();}
 @GetMapping("/api/v1/me") ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt){
  if(!auth.active(jwt.getClaimAsString("sid"),jwt.getSubject()))return ResponseEntity.status(401).build();
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("userId",jwt.getSubject(),"tenantId",jwt.getClaimAsString("tenant_id"),"branchId",jwt.getClaimAsString("branch_id"),"scope",jwt.getClaimAsString("scope")));
 }
 private ResponseEntity<?> response(AuthService.Tokens tokens){return tokens==null?ResponseEntity.status(401).cacheControl(CacheControl.noStore()).body(Map.of("code","AUTHENTICATION_FAILED","message","Sign-in or session could not be verified")):ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(tokens);}
}
