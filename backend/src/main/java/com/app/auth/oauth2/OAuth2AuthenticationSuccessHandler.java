package com.app.auth.oauth2;

import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.app.config.security.JwtTokenProvider;
import com.app.config.security.OAuth2Properties;
import com.app.user.model.User;
import com.app.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Invoked by Spring Security after a successful Google OAuth2 / OIDC login.
 *
 * Responsibilities:
 *  1. Retrieve the authenticated {@link OidcUser} principal.
 *  2. Look up the corresponding {@link User} entity (guaranteed to exist because
 *     {@link CustomOAuth2UserService} already persisted it).
 *  3. Issue a JWT access + refresh token pair.
 *  4. Redirect the browser to the frontend callback URL with tokens as query params.
 *
 * Frontend redirect example:
 * {@code http://localhost:3000/oauth2/callback?accessToken=xxx&refreshToken=yyy}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final OAuth2Properties oauth2Properties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (response.isCommitted()) {
            log.warn("Response already committed — cannot redirect after OAuth2 success");
            return;
        }

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,
                        "OAuth2 user not found after persistence: " + email));

        String accessToken  = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        String redirectUri = UriComponentsBuilder
                .fromUriString(oauth2Properties.getRedirectUri())
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        log.debug("OAuth2 login success for {}; redirecting to frontend", email);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
