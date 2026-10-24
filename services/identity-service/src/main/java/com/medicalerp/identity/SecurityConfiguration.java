package com.medicalerp.identity;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
class SecurityConfiguration {
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  // Tokens are explicitly submitted, never ambient cookies. No cookie authentication.
  return http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health","/api/v1/auth/jwks").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.POST,"/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/logout").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/v1/me").authenticated().anyRequest().denyAll())
   .oauth2ResourceServer(o -> o.jwt(j -> {})).build();
 }
}
