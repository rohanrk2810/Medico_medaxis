package com.medicalerp.gateway;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
class GatewayRoutes {
 @Bean RouteLocator routes(RouteLocatorBuilder builder,@Value("${services.tenant}") String tenant,@Value("${services.identity}") String identity) {
  return builder.routes().route("tenant",r -> r.path("/api/v1/tenants/**").uri(tenant))
   .route("identity",r -> r.path("/api/v1/auth/**","/api/v1/me").uri(identity)).build();
 }
 @Bean GlobalFilter correlation() { return (exchange,chain) -> {
  String trace=UUID.randomUUID().toString();
  exchange.getResponse().getHeaders().set("X-Correlation-ID",trace);
  return chain.filter(exchange.mutate().request(r -> r.headers(h -> {
   h.set("X-Correlation-ID",trace);h.remove("X-Tenant-ID");h.remove("X-User-ID");h.remove("X-Roles");
  })).build());
 };}
}
