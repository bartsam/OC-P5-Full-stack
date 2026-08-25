package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("CustomUserDetailsService integration")
class CustomUserDetailsServiceIntegrationTest {

  @Container
  @ServiceConnection
  static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.4");

  @Autowired
  private CustomUserDetailsService customUserDetailsService;

  @Autowired
  private UserRepository userRepository;

  @AfterEach
  void cleanDatabase() {
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("should load a user by email")
  void shouldLoadUserByEmail() {
    // GIVEN
    userRepository.saveAndFlush(new UserEntity(
        "john.doe@example.com",
        "jeanbiche",
        "encodedPassword123!"));

    // WHEN
    UserDetails userDetails = customUserDetailsService.loadUserByUsername("john.doe@example.com");

    // THEN
    assertThat(userDetails.getUsername()).isEqualTo("john.doe@example.com");
    assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123!");
    assertThat(userDetails.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName("should load a user by username when email does not match")
  void shouldLoadUserByUsernameWhenEmailDoesNotMatch() {
    // GIVEN
    userRepository.saveAndFlush(new UserEntity(
        "john.doe@example.com",
        "jeanbiche",
        "encodedPassword123!"));

    // WHEN
    UserDetails userDetails = customUserDetailsService.loadUserByUsername("jeanbiche");

    // THEN
    assertThat(userDetails.getUsername()).isEqualTo("john.doe@example.com");
    assertThat(userDetails.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName("should throw exception when user does not exist")
  void shouldThrowExceptionWhenUserDoesNotExist() {
    assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("User not found: unknown");
  }
}
