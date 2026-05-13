package com.app.user.service;

import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.app.user.model.UserPrincipal;
import com.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Loads user by UUID string (stored as JWT subject).
 * Registered as the primary {@link UserDetailsService} bean consumed by
 * {@link com.app.config.SecurityConfig}.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findById(UUID.fromString(userId))
                .map(UserPrincipal::new)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,
                        "User not found with id: " + userId));
    }
}
