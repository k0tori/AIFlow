package com.aiflow.common.util;

import com.aiflow.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT utility for generating, parsing, and validating JSON Web Tokens.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:aiflow-platform-secret-key-must-be-at-least-256-bits}")
    private String secret;

    /**
     * Get the signing key from the configured secret.
     *
     * @return HMAC SHA key for JWT signing/verification
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token for the given user.
     *
     * @param userId   the user ID (stored as subject)
     * @param username the username (stored as a claim)
     * @return the signed JWT token string
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + SecurityConstants.TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parse and validate the token, returning the claims.
     *
     * @param token the JWT token string
     * @return the parsed claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate whether the token is well-formed and not expired.
     *
     * @param token the JWT token string
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract the user ID from the token's subject claim.
     *
     * @param token the JWT token string
     * @return the user ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract the username from the token's claims.
     *
     * @param token the JWT token string
     * @return the username
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
}
