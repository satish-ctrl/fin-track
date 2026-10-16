package com.fintrack.service;

import com.fintrack.entity.User;
import com.fintrack.enums.UserStatus;
import com.fintrack.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = userRepository.findByEmailAndStatus(
                email.trim().toLowerCase(java.util.Locale.ROOT),
                UserStatus.ACTIVE
        ).orElseThrow(() ->
                new UsernameNotFoundException("Invalid email or password")
        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_USER")
                .build();
    }
}
