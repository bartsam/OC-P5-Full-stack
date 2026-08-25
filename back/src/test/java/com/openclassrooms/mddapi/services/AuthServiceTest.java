package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

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
  @DisplayName("register")
  class RegisterTests {
    @Test
    @DisplayName("should register user when request is valid")
    void shouldRegisterUserWhenRequestValid() {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      Authentication authentication = new UsernamePasswordAuthenticationToken(
          "john.doe@example.com", "Password123!");

      when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
      when(userRepository.existsByUsername("jeanbiche")).thenReturn(false);
      when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123!");
      when(authenticationManager.authenticate(any()))
          .thenReturn(authentication);
      when(jwtService.generateToken(any(Authentication.class)))
          .thenReturn("fake.jwt.token");

      // WHEN
      AuthResponse response = authService.register(request);

      // THEN
      verify(userRepository).existsByEmail("john.doe@example.com");
      verify(userRepository).existsByUsername("jeanbiche");
      verify(passwordEncoder).encode("Password123!");

      verify(userRepository).save(userCaptor.capture());
      verify(authenticationManager).authenticate(authenticationCaptor.capture());
      verify(jwtService).generateToken(authentication);

      Authentication authenticationRequest = authenticationCaptor.getValue();
      assertThat(authenticationRequest.getName()).isEqualTo("john.doe@example.com");
      assertThat(authenticationRequest.getCredentials()).isEqualTo("Password123!");
      assertThat(response.token()).isEqualTo("fake.jwt.token");

      UserEntity savedUser = userCaptor.getValue();
      assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
      assertThat(savedUser.getUsername()).isEqualTo("jeanbiche");
      assertThat(savedUser.getPassword()).isEqualTo("encodedPassword123!");
      assertThat(savedUser.getId()).isNull();
    }

    @Test
    @DisplayName("should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

      // THEN
      assertThatThrownBy(() -> authService.register(request))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("Email is already in use");

      verify(passwordEncoder, never()).encode(anyString());
      verify(userRepository, never()).save(any(UserEntity.class));
      verify(authenticationManager, never()).authenticate(any());
      verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should not generate token when authentication fails")
    void shouldNotGenerateTokenWhenAuthenticationFails() {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
      when(userRepository.existsByUsername("jeanbiche")).thenReturn(false);
      when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123!");
      when(authenticationManager.authenticate(any()))
          .thenThrow(new BadCredentialsException("Bad credentials"));

      // THEN
      assertThatThrownBy(() -> authService.register(request))
          .isInstanceOf(BadCredentialsException.class);

      verify(userRepository).save(any(UserEntity.class));
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
      // GIVEN
      RegisterRequest request = new RegisterRequest(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
      when(userRepository.existsByUsername("jeanbiche")).thenReturn(true);

      // THEN
      assertThatThrownBy(() -> authService.register(request))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("Username is already in use");

      verify(passwordEncoder, never()).encode(anyString());
      verify(userRepository, never()).save(any(UserEntity.class));
      verify(authenticationManager, never()).authenticate(any());
      verify(jwtService, never()).generateToken(any());
    }
  }

}
