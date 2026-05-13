package com.app.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2Properties {

    /**
     * Frontend URI to redirect to after a successful OAuth2 login.
     * Access and refresh tokens are appended as query parameters:
     * {@code ?accessToken=...&refreshToken=...}
     */
    private String redirectUri;
}
