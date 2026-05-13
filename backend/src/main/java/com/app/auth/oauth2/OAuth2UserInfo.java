package com.app.auth.oauth2;

import lombok.Builder;
import lombok.Getter;

/**
 * Normalized user info extracted from any OAuth2/OIDC provider.
 * Keeps provider-specific attribute extraction in one place.
 */
@Getter
@Builder
public class OAuth2UserInfo {

    private final String providerId;   // Google "sub"
    private final String email;
    private final String fullName;

    /**
     * Extract from Google OIDC standard claims.
     * Google attribute map keys: "sub", "email", "name", "picture".
     */
    public static OAuth2UserInfo fromGoogle(java.util.Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .providerId(String.valueOf(attributes.get("sub")))
                .email(String.valueOf(attributes.get("email")))
                .fullName(String.valueOf(attributes.getOrDefault("name", "")))
                .build();
    }
}
