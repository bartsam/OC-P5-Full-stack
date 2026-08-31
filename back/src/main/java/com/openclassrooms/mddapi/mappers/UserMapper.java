package com.openclassrooms.mddapi.mappers;

import org.mapstruct.Mapper;

import com.openclassrooms.mddapi.dto.UserResponse;
import com.openclassrooms.mddapi.models.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

  public UserResponse toDto(UserEntity user);
}