package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.mddapi.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UserService")
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  @Nested
  @DisplayName("findByEmailOrUsername")
  class FindByEmailOrUsernameTests {

    @Test
    @DisplayName("should return user when found by email")
    void findByEmailOrUsername_shouldReturnUser_whenFoundByEmail() {
      // GIVEN
      UserEntity user = new UserEntity(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      when(userRepository.findByEmail("john.doe@example.com"))
          .thenReturn(Optional.of(user));

      // WHEN
      UserEntity result = userService.findByEmailOrUsername("john.doe@example.com");

      // THEN
      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
      assertThat(result.getUsername()).isEqualTo("jeanbiche");
    }

    @Test
    @DisplayName("should return user when found by username")
    void findByEmailOrUsername_shouldReturnUser_whenFoundByUsername() {
      // GIVEN
      UserEntity user = new UserEntity(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");

      when(userRepository.findByEmail("jeanbiche"))
          .thenReturn(Optional.empty());

      when(userRepository.findByUsername("jeanbiche"))
          .thenReturn(Optional.of(user));

      // WHEN
      UserEntity result = userService.findByEmailOrUsername("jeanbiche");

      // THEN
      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
      assertThat(result.getUsername()).isEqualTo("jeanbiche");
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void findByEmailOrUsername_shouldThrowException_whenNotFound() {
      // GIVEN
      when(userRepository.findByEmail("unknown"))
          .thenReturn(Optional.empty());
      when(userRepository.findByUsername("unknown"))
          .thenReturn(Optional.empty());

      // WHEN + THEN
      assertThatThrownBy(() -> userService.findByEmailOrUsername("unknown"))
          .isInstanceOf(UserNotFoundException.class)
          .hasMessage("User not found");
    }
  }

  @Nested
  @DisplayName("updateUser")
  class updateUserTests {

    @Test
    @DisplayName("should update email, username and password when data is valid")
    void updateUser_shouldUpdateUser_whenDataValid() {
      // GIVEN
      String identifier = "john.doe@example.com";
      UserEntity existingUser = new UserEntity(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");
      existingUser.setId(1L);

      UpdateUserRequest request = new UpdateUserRequest(
          "new.john@example.com",
          "newjeanbiche",
          "NewPassword123!");

      UserEntity updatedUser = new UserEntity(
          "new.john@example.com",
          "newjeanbiche",
          "EncodedNewPassword123!");
      updatedUser.setId(1L);

      when(userRepository.findByEmail(identifier))
          .thenReturn(Optional.of(existingUser));

      when(userRepository.existsByEmailAndIdNot("new.john@example.com", 1L))
          .thenReturn(false);

      when(userRepository.existsByUsernameAndIdNot("newjeanbiche", 1L))
          .thenReturn(false);

      when(passwordEncoder.encode("NewPassword123!"))
          .thenReturn("EncodedNewPassword123!");

      when(userRepository.save(any(UserEntity.class)))
          .thenReturn(updatedUser);

      // WHEN
      UserEntity result = userService.updateUser(identifier, request);

      // THEN
      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isEqualTo("new.john@example.com");
      assertThat(result.getUsername()).isEqualTo("newjeanbiche");

      verify(userRepository).findByEmail(identifier);
      verify(userRepository).existsByEmailAndIdNot("new.john@example.com", 1L);
      verify(userRepository).existsByUsernameAndIdNot("newjeanbiche", 1L);
      verify(passwordEncoder).encode("NewPassword123!");
      verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("should throw UserNotFoundException when user does not exist")
    void updateUser_shouldThrowUserNotFoundException_whenUserNotFound() {
      // GIVEN
      String identifier = "unknown@example.com";
      UpdateUserRequest request = new UpdateUserRequest(
          "new.john@example.com",
          "newjeanbiche",
          "NewPassword123!");

      when(userRepository.findByEmail(identifier))
          .thenReturn(Optional.empty());

      when(userRepository.findByUsername(identifier))
          .thenReturn(Optional.empty());

      // THEN
      assertThatThrownBy(() -> userService.updateUser(identifier, request))
          .isInstanceOf(UserNotFoundException.class)
          .hasMessage("User not found");
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when email is already in use")
    void updateUser_shouldThrowUserAlreadyExistsException_whenEmailExists() {
      // GIVEN
      String identifier = "john.doe@example.com";
      UserEntity existingUser = new UserEntity(
          "john.doe@example.com",
          "jeanbiche",
          "Password123!");
      existingUser.setId(1L);

      UpdateUserRequest request = new UpdateUserRequest(
          "duplicate@example.com",
          "newjeanbiche",
          "NewPassword123!");

      when(userRepository.findByEmail(identifier))
          .thenReturn(Optional.of(existingUser));

      when(userRepository.existsByEmailAndIdNot("duplicate@example.com", 1L))
          .thenReturn(true);

      // THEN
      assertThatThrownBy(() -> userService.updateUser(identifier, request))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("Email is already in use");
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when username is already in use")
    void updateUser_shouldThrowUserAlreadyExistsException_whenUsernameExists() {
      // GIVEN
      String identifier = "john.doe@example.com";
      UserEntity existingUser = new UserEntity(
          "john.doe@example.com",
          "jeanbiche",
          "OldPassword123!");
      existingUser.setId(1L);

      UpdateUserRequest request = new UpdateUserRequest(
          "new.john@example.com",
          "duplicateUser",
          "NewPassword123!");

      when(userRepository.findByEmail(identifier))
          .thenReturn(Optional.of(existingUser));

      when(userRepository.existsByEmailAndIdNot("new.john@example.com", 1L))
          .thenReturn(false);

      when(userRepository.existsByUsernameAndIdNot("duplicateUser", 1L))
          .thenReturn(true);

      // WHEN + THEN
      assertThatThrownBy(() -> userService.updateUser(identifier, request))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("Username is already in use");
    }
  }
}