package com.openclassrooms.mddapi.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.openclassrooms.mddapi.dto.UserResponse;
import com.openclassrooms.mddapi.models.UserEntity;

@Tag("unit")
@DisplayName("UserMapper")
class UserMapperTest {

  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  @Test
  @DisplayName("should map all profile fields when user is provided")
  void shouldMapUserEntityToUserResponse() {
    // GIVEN
    LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
    LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 1, 11, 0);

    UserEntity user = new UserEntity(
        "john.doe@example.com",
        "jeanbiche",
        "encodedPassword");

    user.setId(1L);
    user.setCreatedAt(createdAt);
    user.setUpdatedAt(updatedAt);

    // WHEN
    UserResponse response = userMapper.toDto(user);

    // THEN
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.email()).isEqualTo("john.doe@example.com");
    assertThat(response.username()).isEqualTo("jeanbiche");
    assertThat(response.createdAt()).isEqualTo(createdAt);
    assertThat(response.updatedAt()).isEqualTo(updatedAt);
  }

  @Test
  @DisplayName("should return null when user is null")
  void shouldReturnNullWhenUserIsNull() {
    assertThat(userMapper.toDto(null)).isNull();
  }
}
