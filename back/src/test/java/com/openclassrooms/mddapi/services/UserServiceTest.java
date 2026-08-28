package com.openclassrooms.mddapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.openclassrooms.mddapi.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UserService")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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
}