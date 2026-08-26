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

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

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

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("Email is already in use");
    }
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new UserAlreadyExistsException("Username is already in use");
    }

    UserEntity user = new UserEntity(
        request.getEmail(),
        request.getUsername(),
        passwordEncoder.encode(request.getPassword()));

    userRepository.save(user);

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    String token = jwtService.generateToken(authentication);
    return new AuthResponse(token);
  }

  public AuthResponse login(LoginRequest request) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));

    String token = jwtService.generateToken(authentication);
    return new AuthResponse(token);

  }
}