package com.openclassrooms.mddapi.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.exceptions.UserNotFoundException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

@Service
public class UserService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UserEntity findByEmailOrUsername(String identifier) {

    return userRepository.findByEmail(identifier)
        .or(() -> userRepository.findByUsername(identifier))
        .orElseThrow(() -> new UserNotFoundException("User not found"));

  }

  public UserEntity updateProfile(String identifier, UpdateUserRequest request) {
    UserEntity user = findByEmailOrUsername(identifier);

    if (userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
      throw new UserAlreadyExistsException("Email is already in use");
    }
    if (userRepository.existsByUsernameAndIdNot(request.username(), user.getId())) {
      throw new UserAlreadyExistsException("Username is already in use");
    }

    user.setEmail(request.email());
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));

    return userRepository.save(user);
  }

}
