package com.app.auth.oauth2;

import com.app.config.security.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Invoked by Spring Security when OAuth2 / OIDC login fails.
 * Redirects the browser to the frontend callback URL with an {@code error} query parameter.
 *
 * Example: {@code http://localhost:3000/oauth2/callback?error=OAuth2+authentication+failed}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2Properties oauth2Properties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("OAuth2 authentication failed: {}", exception.getMessage());

        String errorMessage = URLEncoder.encode(exception.getLocalizedMessage(), StandardCharsets.UTF_8);

        String redirectUri = UriComponentsBuilder
                .fromUriString(oauth2Properties.getRedirectUri())
                .queryParam("error", errorMessage)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
