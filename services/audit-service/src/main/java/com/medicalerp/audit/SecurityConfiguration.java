package com.medicalerp.audit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
class SecurityConfiguration {
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  // Bearer-only API: no cookie authentication. Login/session transport is a later slice.
  return http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health").permitAll().anyRequest().denyAll())
   .build();
 }
}
