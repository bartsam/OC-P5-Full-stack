package com.openclassrooms.mddapi.services;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserEntity findByEmailOrUsername(String identifier) {

    return userRepository.findByEmail(identifier)
        .or(() -> userRepository.findByUsername(identifier))
        .orElseThrow(() -> new UserNotFoundException("User not found"));

  }

}
