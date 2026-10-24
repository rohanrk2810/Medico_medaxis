package com.medicalerp.tenant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
class SecurityConfiguration {
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  return http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/v1/tenants/*").hasAuthority("SCOPE_tenant:read")
    .anyRequest().denyAll())
   .oauth2ResourceServer(o -> o.jwt(j -> {})).build();
 }
}
