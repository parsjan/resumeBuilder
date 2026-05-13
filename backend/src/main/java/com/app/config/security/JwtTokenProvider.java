package com.app.config.security;

import com.app.common.AppException;
import com.app.common.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // ── Token generation ──────────────────────────────────────────────────

    public String generateAccessToken(UUID userId, String email) {
        return buildToken(userId, email, jwtProperties.getExpirationMs());
    }

    public String generateRefreshToken(UUID userId, String email) {
        return buildToken(userId, email, jwtProperties.getRefreshExpirationMs());
    }

    private String buildToken(UUID userId, String email, long ttlMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMs))
                .signWith(signingKey())
                .compact();
    }

    // ── Token validation ──────────────────────────────────────────────────

    public Claims validateAndParseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.debug("JWT expired: {}", ex.getMessage());
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT invalid: {}", ex.getMessage());
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(validateAndParseClaims(token).getSubject());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
