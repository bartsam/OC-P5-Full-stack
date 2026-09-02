package com.openclassrooms.mddapi.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtEncoder jwtEncoder;

  /**
   * Constructs the JwtService with the required Spring Security JwtEncoder.
   *
   * @param jwtEncoder the encoder used to build and sign JWT tokens
   */
  public JwtService(JwtEncoder jwtEncoder) {
    this.jwtEncoder = jwtEncoder;
  }

  /**
   * Generates a JWT token for an authenticated user.
   *
   * @param authentication Spring Security Authentication with CustomUserDetails
   * @return the encoded JWT token string
   */
  public String generateToken(Authentication authentication) {
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    return generateToken(principal.getId());
  }

  /**
   * Generates a JWT token signed with HS256 for a specific user ID.
   *
   * @param userId ID of the user to set as the token subject
   * @return the encoded JWT token string
   */
  public String generateToken(Long userId) {
    Instant now = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(1, ChronoUnit.DAYS))
        .subject(String.valueOf(userId))
        .build();

    JwtEncoderParameters params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
        claims);

    return jwtEncoder.encode(params).getTokenValue();
  }

}
