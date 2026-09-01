package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

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

  @Captor
  private ArgumentCaptor<UserEntity> userCaptor;

  @Nested
  @Tag("findById")
  @DisplayName("Find by id")
  class FindByIdTests {

    @Test
    @DisplayName("should return the user when id exists")
    void findById_shouldReturnUser_whenIdExists() {
      // GIVEN
      UserEntity user = new UserEntity(
          "john.doe@example.com", "jeanbiche", "encodedPassword123!");
      user.setId(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));

      // WHEN
      UserEntity result = userService.findById(1L);

      // THEN
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("should throw EntityNotFoundException when id does not exist")
    void findById_shouldThrowException_whenIdDoesNotExist() {
      // GIVEN
      when(userRepository.findById(99L)).thenReturn(Optional.empty());

      // THEN
      assertThatThrownBy(() -> userService.findById(99L))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessage("User not found with id: 99");
    }
  }

  @Nested
  @Tag("updateUser")
  @DisplayName("Update user")
  class UpdateUserTests {

    @Test
    @DisplayName("should update email, username and encoded password when request is valid")
    void updateUser_shouldUpdateUser_whenRequestValid() {
      // GIVEN
      UserEntity existingUser = new UserEntity(
          "old.email@example.com", "oldUsername", "oldEncodedPassword");
      existingUser.setId(1L);

      UpdateUserRequest request = new UpdateUserRequest(
          "new.email@example.com", "newUsername", "NewPassword123!");

      when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
      when(userRepository.existsByEmailAndIdNot(request.email(), 1L)).thenReturn(false);
      when(userRepository.existsByUsernameAndIdNot(request.username(), 1L)).thenReturn(false);
      when(passwordEncoder.encode(request.password())).thenReturn("newEncodedPassword");
      when(userRepository.save(any(UserEntity.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // WHEN
      UserEntity result = userService.updateUser(1L, request);

      // THEN
      assertThat(result.getEmail()).isEqualTo("new.email@example.com");
      assertThat(result.getUsername()).isEqualTo("newUsername");
      assertThat(result.getPassword()).isEqualTo("newEncodedPassword");

      verify(userRepository).save(userCaptor.capture());
      UserEntity savedUser = userCaptor.getValue();
      assertThat(savedUser.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should throw EntityNotFoundException when user to update does not exist")
    void updateUser_shouldThrowException_whenUserDoesNotExist() {
      // GIVEN
      UpdateUserRequest request = new UpdateUserRequest(
          "new.email@example.com", "newUsername", "NewPassword123!");
      when(userRepository.findById(99L)).thenReturn(Optional.empty());

      // THEN
      assertThatThrownBy(() -> userService.updateUser(99L, request))
          .isInstanceOf(EntityNotFoundException.class);

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when email is used by another user")
    void updateUser_shouldThrowException_whenEmailUsedByAnotherUser() {
      // GIVEN
      UserEntity existingUser = new UserEntity(
          "old.email@example.com", "oldUsername", "oldEncodedPassword");
      existingUser.setId(1L);

      UpdateUserRequest request = new UpdateUserRequest(
          "taken.email@example.com", "newUsername", "NewPassword123!");

      when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
      when(userRepository.existsByEmailAndIdNot(request.email(), 1L)).thenReturn(true);

      // THEN
      assertThatThrownBy(() -> userService.updateUser(1L, request))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("Email is already in use");

      verify(userRepository, never()).save(any());
      verify(userRepository, never()).existsByUsernameAndIdNot(any(), anyLong());
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when username is used by another user")
    void updateUser_shouldThrowException_whenUsernameUsedByAnotherUser() {
      // GIVEN
      UserEntity existingUser = new UserEntity(
          "old.email@example.com", "oldUsername", "oldEncodedPassword");
      existingUser.setId(1L);

      UpdateUserRequest request = new UpdateUserRequest(
          "new.email@example.com", "takenUsername", "NewPassword123!");

      when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
      when(userRepository.existsByEmailAndIdNot(request.email(), 1L)).thenReturn(false);
      when(userRepository.existsByUsernameAndIdNot(request.username(), 1L)).thenReturn(true);

      // THEN
      assertThatThrownBy(() -> userService.updateUser(1L, request))
          .isInstanceOf(UserAlreadyExistsException.class)
          .hasMessage("Username is already in use");

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should allow update when email and username are unchanged")
    void updateUser_shouldAllowUpdate_whenEmailAndUsernameUnchanged() {
      // GIVEN
      UserEntity existingUser = new UserEntity(
          "john.doe@example.com", "jeanbiche", "oldEncodedPassword");
      existingUser.setId(1L);

      UpdateUserRequest request = new UpdateUserRequest(
          "john.doe@example.com", "jeanbiche", "NewPassword123!");

      when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
      when(userRepository.existsByEmailAndIdNot(request.email(), 1L)).thenReturn(false);
      when(userRepository.existsByUsernameAndIdNot(request.username(), 1L)).thenReturn(false);
      when(passwordEncoder.encode(request.password())).thenReturn("newEncodedPassword");
      when(userRepository.save(any(UserEntity.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // WHEN
      UserEntity result = userService.updateUser(1L, request);

      // THEN
      assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
      assertThat(result.getUsername()).isEqualTo("jeanbiche");
    }
  }
}