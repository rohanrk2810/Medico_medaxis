package com.medicalerp.identity;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration @Profile("synthetic-dev")
class DevelopmentBootstrap {
 @Bean CommandLineRunner syntheticUser(JdbcTemplate db,PasswordEncoder passwords,@Value("${DEV_TENANT_ID}") UUID tenant,@Value("${DEV_BRANCH_ID}") UUID branch,@Value("${DEV_USER_ID}") UUID user,@Value("${DEV_PASSWORD}") String password){
  return args -> {
   if(password.length()<16 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalArgumentException("Development password must be 16+ characters and at most 72 UTF-8 bytes");
   db.update("INSERT INTO users(id,tenant_id,email,password_hash) VALUES (?,?,?,?) ON CONFLICT DO NOTHING",user,tenant,"admin@example.test",passwords.encode(password));
   db.update("INSERT INTO user_grants(user_id,branch_id,permission) VALUES (?,?,?) ON CONFLICT DO NOTHING",user,branch,"tenant:read");
  };
 }
}
