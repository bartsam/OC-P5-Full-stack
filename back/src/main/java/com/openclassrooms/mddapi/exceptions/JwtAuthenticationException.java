package com.openclassrooms.mddapi.exceptions;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.openclassrooms.mddapi.dto.MessageResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles unauthenticated API requests by returning a custom 401 Unauthorized
 */
@Component
public class JwtAuthenticationException implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public JwtAuthenticationException(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException {

    // Return 401 with JSON error payload when JWT authentication fails or missing
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setHeader("WWW-Authenticate", "Bearer realm=\"api\"");

    objectMapper.writeValue(
        response.getOutputStream(),
        new MessageResponse("Authentication required: invalid or missing token"));
  }
}