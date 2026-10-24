package com.medicalerp.identity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.Base64;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
@Configuration
class TokenConfiguration {
 @Bean RSAKey signingKey(@Value("${auth.private-key-file}") String privateFile,@Value("${auth.public-key-file}") String publicFile) throws Exception {
  KeyFactory factory=KeyFactory.getInstance("RSA");
  var privateKey=(RSAPrivateKey)factory.generatePrivate(new PKCS8EncodedKeySpec(pem(privateFile)));
  var publicKey=(RSAPublicKey)factory.generatePublic(new X509EncodedKeySpec(pem(publicFile)));
  if(publicKey.getModulus().bitLength()<2048 || !publicKey.getModulus().equals(privateKey.getModulus())) throw new IllegalArgumentException("Invalid signing key pair");
  return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("identity-v1").build();
 }
 private byte[] pem(String path) throws Exception {return Base64.getDecoder().decode(Files.readString(Path.of(path)).replaceAll("-----[^-]+-----", "").replaceAll("\\s", ""));}
 @Bean JwtEncoder encoder(RSAKey key) {return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));}
 @Bean JwtDecoder decoder(RSAKey key,@Value("${auth.issuer}") String issuer) throws Exception {
  NimbusJwtDecoder decoder=NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).build();
  decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));return decoder;
 }
 @Bean PasswordEncoder passwords(){return new BCryptPasswordEncoder(12);}
}
