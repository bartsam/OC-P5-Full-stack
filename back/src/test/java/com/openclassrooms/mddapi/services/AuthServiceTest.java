package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("AuthService")
public class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtService jwtService;

  @InjectMocks
  private AuthService authService;

  @Captor
  private ArgumentCaptor<UserEntity> userCaptor;

  @Captor
  private ArgumentCaptor<Authentication> authenticationCaptor;

  @Nested
  @Tag("register")
  @DisplayName("Register")
  class RegisterTests {
    @Test
    @DisplayName("should create the user and generate a token based on the user ID")
    void register_shouldCreateUserAndReturnToken_whenRequestValid() {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      when(userRepository.existsByEmail(request.email())).thenReturn(false);
      when(userRepository.existsByUsername(request.username())).thenReturn(false);
      when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword123!");

      UserEntity savedUser = new UserEntity(request.email(), request.username(), "encodedPassword123!");
      savedUser.setId(1L);
      when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

      when(jwtService.generateToken(1L)).thenReturn("fake.jwt.token");

      // WHEN
      AuthResponse response = authService.register(request);

      // THEN
      assertThat(response.token()).isEqualTo("fake.jwt.token");

      verify(userRepository).save(userCaptor.capture());
      UserEntity newUser = userCaptor.getValue();
      assertThat(newUser.getPassword()).isEqualTo("encodedPassword123!");

      verify(jwtService).generateToken(1L);
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when email already exists")
    void register_shouldThrowException_whenEmailAlreadyExists() {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");
      when(userRepository.existsByEmail(request.email())).thenReturn(true);

      // THEN
      assertThatThrownBy(() -> authService.register(request))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("Email is already in use");
      verify(userRepository, never()).save(any());
      verify(jwtService, never()).generateToken(any(Long.class));
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when username already exists")
    void register_shouldThrowException_whenUsernameAlreadyExists() {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");
      when(userRepository.existsByEmail(request.email())).thenReturn(false);
      when(userRepository.existsByUsername(request.username())).thenReturn(true);

      // THEN
      assertThatThrownBy(() -> authService.register(request))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("Username is already in use");

      verify(userRepository, never()).save(any());
    }
  }

  @Nested
  @Tag("login")
  @DisplayName("Login")
  class LoginTests {
    @Test
    @DisplayName("should authenticate user and return token when credentials are valid")
    void login_shouldAuthenticateUser_whenCredentialsValid() {
      // GIVEN
      LoginRequest request = new LoginRequest(
          "john.doe@example.com",
          "Password123!");
      Authentication authResult = new UsernamePasswordAuthenticationToken(
          "john.doe@example.com", "Password123!");

      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenReturn(authResult);
      when(jwtService.generateToken(authResult)).thenReturn("fake.jwt.token");

      // WHEN
      AuthResponse response = authService.login(request);

      // THEN
      assertThat(response.token()).isEqualTo("fake.jwt.token");

      verify(jwtService).generateToken(authResult);
    }

    @Test
    @DisplayName("should throw BadCredentialsException when credentials are invalid")
    void login_shouldThrowException_whenCredentialsInvalid() {
      // GIVEN
      LoginRequest request = new LoginRequest(
          "john.doe@example.com",
          "wrongPassword123!");

      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenThrow(new BadCredentialsException("Bad credentials"));

      // THEN
      assertThatThrownBy(() -> authService.login(request))
          .isInstanceOf(BadCredentialsException.class);

      verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("should pass exact credentials to authentication manager")
    void login_shouldPassExactCredentialsToAuthenticationManager() {
      // GIVEN
      LoginRequest request = new LoginRequest(
          "john.doe@example.com",
          "Password123!");

      Authentication authResult = new UsernamePasswordAuthenticationToken(
          "john.doe@example.com",
          "Password123!");

      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenReturn(authResult);
      when(jwtService.generateToken(authResult)).thenReturn("jwt-token");

      // WHEN
      authService.login(request);

      // THEN
      verify(authenticationManager).authenticate(authenticationCaptor.capture());
      Authentication authRequest = authenticationCaptor.getValue();

      assertThat(authRequest.getPrincipal()).isEqualTo(request.identifier());
      assertThat(authRequest.getCredentials()).isEqualTo(request.password());
    }
  }
}