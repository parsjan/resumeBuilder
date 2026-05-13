package com.app.config;

import com.app.auth.oauth2.CustomOAuth2UserService;
import com.app.auth.oauth2.OAuth2AuthenticationFailureHandler;
import com.app.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import com.app.config.security.JwtAuthenticationFilter;
import com.app.user.service.EmailUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // ── Injected beans ────────────────────────────────────────────────────

    /**
     * Loads UserDetails by email — used by the {@link DaoAuthenticationProvider}
     * that handles the /auth/login credential check.
     * ({@link com.app.user.service.CustomUserDetailsService} loads by UUID and is
     * injected directly into {@link JwtAuthenticationFilter} — not needed here.)
     */
    private final EmailUserDetailsService emailUserDetailsService;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    // OAuth2
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    // ── Public routes ─────────────────────────────────────────────────────

    private static final String[] PUBLIC_MATCHERS = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/auth/logout",
            "/oauth2/**",
            "/login/oauth2/**",       // Spring's internal OAuth2 redirect handler
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    // ── Security filter chain ─────────────────────────────────────────────

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // ── CSRF / CORS ──────────────────────────────────────────
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // ── Stateless session ────────────────────────────────────
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── Authorization rules ──────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())

                // ── Local credential authentication ──────────────────────
                .authenticationProvider(authenticationProvider())

                // ── JWT filter (runs before UsernamePasswordAuthFilter) ──
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // ── Return 401 JSON instead of redirecting to login page ──
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint()))

                // ── Google OAuth2 / OIDC ─────────────────────────────────
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(ep -> ep
                                .baseUri("/oauth2/authorize"))
                        .redirectionEndpoint(ep -> ep
                                .baseUri("/login/oauth2/callback/*"))
                        .userInfoEndpoint(ep -> ep
                                .oidcUserService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))

                .build();
    }

    // ── Authentication provider (email + password for /auth/login) ────────

    /**
     * Uses {@link EmailUserDetailsService} so that the "username" passed to
     * {@code AuthenticationManager.authenticate()} can be an email address.
     * {@link com.app.user.service.CustomUserDetailsService} (UUID-based) is wired
     * separately into {@link JwtAuthenticationFilter} and is not used here.
     */
    @Bean
    AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}}");
        };
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(emailUserDetailsService::loadUserByEmail);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
