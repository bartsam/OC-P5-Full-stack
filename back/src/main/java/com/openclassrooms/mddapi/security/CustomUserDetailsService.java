package com.openclassrooms.mddapi.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Constructs the CustomUserDetailsService with the required UserRepository
   *
   * @param userRepository Repository used to locate user entities
   */
  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Loads user by email or username to build Spring Security UserDetails instance
   *
   * @param identifier Email or username identifying the user
   * @return a {@link UserDetails} instance wrapped in a {@link CustomUserDetails}
   * @throws UsernameNotFoundException if user is not found with the identifier
   */
  @Override
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByEmail(identifier)
        .or(() -> userRepository.findByUsername(identifier))
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));

    return CustomUserDetails.builder()
        .id(user.getId())
        .username(user.getUsername())
        .password(user.getPassword())
        .build();
  }
}