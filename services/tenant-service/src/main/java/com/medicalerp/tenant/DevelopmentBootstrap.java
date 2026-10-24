package com.medicalerp.tenant;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration @Profile("synthetic-dev")
class DevelopmentBootstrap {
 @Bean CommandLineRunner syntheticTenant(JdbcTemplate db,@Value("${DEV_TENANT_ID}") UUID tenant,@Value("${DEV_BRANCH_ID}") UUID branch){return args -> {
  db.update("INSERT INTO tenants(id,code,legal_name,country,timezone,currency,status) VALUES (?,'SYNTHETIC','Synthetic Demo Hospital','IN','Asia/Kolkata','INR','ACTIVE') ON CONFLICT DO NOTHING",tenant);
  db.update("INSERT INTO branches(id,tenant_id,code,name) VALUES (?,?,'DEMO','Synthetic Branch') ON CONFLICT DO NOTHING",branch,tenant);
 };}
}
