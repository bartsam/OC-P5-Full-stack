package com.openclassrooms.mddapi.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  /**
   * Constructs the AuthService with required authentication dependencies.
   *
   * @param userRepository        Repository for managing UserEntity persistence
   * @param passwordEncoder       Encoder used for hashing passwords
   * @param authenticationManager Spring Security authentication manager
   * @param jwtService            Service responsible for generating JWT tokens
   */
  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  /**
   * Registers a new user, authenticates them, and return a JWT token.
   *
   * @param request Registration details with email, username, and raw password
   * @return an {@link AuthResponse} containing the generated JWT token
   * @throws UserAlreadyExistsException if email or username already registered
   */
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new UserAlreadyExistsException("Email is already in use");
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new UserAlreadyExistsException("Username is already in use");
    }

    UserEntity newUser = userRepository.save(new UserEntity(
        request.email(),
        request.username(),
        passwordEncoder.encode(request.password())));

    String token = jwtService.generateToken(newUser.getId());

    return new AuthResponse(token);
  }

  /**
   * Authenticates a user using their credentials and issues a JWT token.
   *
   * @param request Login payload with user identifier and password
   * @return an {@link AuthResponse} containing the generated JWT token
   * @throws EntityNotFoundException if authenticated user cannot be retrieved
   */
  public AuthResponse login(LoginRequest request) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

    String token = jwtService.generateToken(authentication);
    return new AuthResponse(token);

  }
}