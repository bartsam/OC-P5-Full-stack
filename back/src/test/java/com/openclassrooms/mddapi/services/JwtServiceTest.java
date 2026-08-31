package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Tag("unit")
@DisplayName("JwtService")
class JwtServiceTest {

  private static final String JWT_SECRET = "test-secret-key-with-at-least-32-characters";

  private JwtService jwtService;
  private JwtDecoder jwtDecoder;

  @BeforeEach
  void setUp() {
    byte[] secret = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
    JwtEncoder jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secret));

    jwtService = new JwtService(jwtEncoder);
    jwtDecoder = NimbusJwtDecoder
        .withSecretKey(new SecretKeySpec(secret, "HmacSHA256"))
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }

  @Test
  @DisplayName("should generate a valid JWT with the authenticated user as subject")
  void shouldGenerateValidJwtForAuthenticatedUser() {
    // GIVEN
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("john.doe@example.com",
        "Password123!");

    // WHEN
    String token = jwtService.generateToken(authentication);
    Jwt jwt = jwtDecoder.decode(token);

    // THEN
    assertThat(token).isNotBlank();
    assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
    assertThat(jwt.getSubject()).isEqualTo("john.doe@example.com");
    assertThat(jwt.getClaimAsString("iss")).isEqualTo("self");
    assertThat(jwt.getIssuedAt()).isNotNull();
    assertThat(jwt.getExpiresAt()).isEqualTo(jwt.getIssuedAt().plus(1, ChronoUnit.DAYS));
  }
}
