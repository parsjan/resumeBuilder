package com.app.user.service;

import com.app.user.model.UserPrincipal;
import com.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads {@link UserDetails} by email address.
 *
 * Used exclusively by the {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
 * wired inside {@link com.app.config.SecurityConfig}, which receives email as the
 * "username" during form/API login.
 *
 * NOT the primary {@link org.springframework.security.core.userdetails.UserDetailsService} bean —
 * that role belongs to {@link CustomUserDetailsService}, which loads by UUID for the JWT filter.
 */
@Component
@RequiredArgsConstructor
public class EmailUserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));
    }
}
