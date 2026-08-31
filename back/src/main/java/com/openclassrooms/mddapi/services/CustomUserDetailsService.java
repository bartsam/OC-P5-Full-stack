package com.openclassrooms.mddapi.services;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

/**
 * Loads user details from database for Spring Security authentication
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // Fetches user by email or username, return UserDetails with default user roles
  @Override
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByEmail(identifier)
        .orElseGet(() -> userRepository.findByUsername(identifier)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier)));

    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    return new User(
        user.getEmail(),
        user.getPassword(),
        authorities);
  }
}