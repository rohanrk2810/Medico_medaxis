package com.medicalerp.tenant;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Component
class SessionAccess {
 private final RestClient client;
 SessionAccess(@Value("${identity.session-url}") String url) {
  var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
  factory.setReadTimeout(Duration.ofSeconds(3));
  client=RestClient.builder().baseUrl(url).requestFactory(factory).build();
 }
 void requireActive(Jwt jwt){
  try {var response=client.get().header("Authorization","Bearer "+jwt.getTokenValue()).retrieve().toBodilessEntity();
   if(response.getStatusCode().value()!=200)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Session verification unavailable");
  }
  catch(org.springframework.web.client.HttpClientErrorException.Unauthorized e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Session expired");}
  catch(org.springframework.web.client.RestClientException e){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Session verification unavailable");}
 }
}
