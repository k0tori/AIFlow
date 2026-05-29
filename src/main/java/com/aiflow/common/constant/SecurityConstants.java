package com.aiflow.common.constant;

/**
 * Security-related constants for JWT authentication.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Utility class, no instantiation
    }

    /**
     * JWT token prefix used in the Authorization header.
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * HTTP header name for JWT token.
     */
    public static final String HEADER_NAME = "Authorization";

    /**
     * Token expiration time in milliseconds (24 hours).
     */
    public static final long TOKEN_EXPIRATION = 24 * 60 * 60 * 1000;
}
