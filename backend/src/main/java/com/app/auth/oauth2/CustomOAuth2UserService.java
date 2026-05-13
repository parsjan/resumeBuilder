package com.app.auth.oauth2;

import com.app.user.model.User;
import com.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom OIDC user service for Google sign-in.
 *
 * Flow:
 * 1. Delegate to Spring's {@link OidcUserService} to validate the token and fetch user info.
 * 2. Extract {@link OAuth2UserInfo} from Google's claims.
 * 3. Find an existing {@link User} by email or create a new one (first login).
 * 4. Return the original {@link OidcUser} — the {@link User} entity is now persisted and
 *    will be looked up by email in {@link OAuth2AuthenticationSuccessHandler}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;

    private final OidcUserService delegate = new OidcUserService();

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = resolveUserInfo(registrationId, oidcUser);
        findOrCreateUser(userInfo);

        return oidcUser;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private OAuth2UserInfo resolveUserInfo(String registrationId, OidcUser oidcUser) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return OAuth2UserInfo.fromGoogle(oidcUser.getAttributes());
        }
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
    }

    private void findOrCreateUser(OAuth2UserInfo info) {
        userRepository.findByEmail(info.getEmail()).ifPresentOrElse(
                existing -> {
                    // Update providerId if this account was previously LOCAL
                    if (existing.getProviderId() == null) {
                        existing.setProviderId(info.getProviderId());
                        existing.setProvider(User.Provider.GOOGLE);
                        userRepository.save(existing);
                        log.info("Linked Google OAuth2 to existing account: {}", info.getEmail());
                    }
                },
                () -> {
                    User newUser = User.builder()
                            .email(info.getEmail())
                            .fullName(info.getFullName())
                            .provider(User.Provider.GOOGLE)
                            .providerId(info.getProviderId())
                            .build();
                    userRepository.save(newUser);
                    log.info("Created new user via Google OAuth2: {}", info.getEmail());
                }
        );
    }
}
