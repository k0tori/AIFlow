package com.aiflow.common.util;

import com.aiflow.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JwtUtil.
 */
@SpringBootTest(
        classes = JwtUtilTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Import(JwtUtil.class)
@TestPropertySource(properties = {
        "jwt.secret=aiflow-platform-secret-key-must-be-at-least-256-bits"
})
class JwtUtilTest {

    @org.springframework.context.annotation.Configuration
    @Import(JwtUtil.class)
    static class TestConfig {
    }

    @Autowired
    private JwtUtil jwtUtil;

    private static final Long TEST_USER_ID = 42L;
    private static final String TEST_USERNAME = "testuser";

    @Test
    void shouldGenerateAndParseToken() {
        // Generate a token
        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_USERNAME);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Parse the token and verify claims
        Claims claims = jwtUtil.parseToken(token);

        assertEquals(String.valueOf(TEST_USER_ID), claims.getSubject());
        assertEquals(TEST_USERNAME, claims.get("username", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        // Verify expiration is approximately 24 hours from now
        long expectedExpiration = claims.getIssuedAt().getTime() + SecurityConstants.TOKEN_EXPIRATION;
        assertEquals(expectedExpiration, claims.getExpiration().getTime());
    }

    @Test
    void shouldValidateToken() {
        // Valid token should pass validation
        String validToken = jwtUtil.generateToken(TEST_USER_ID, TEST_USERNAME);
        assertTrue(jwtUtil.validateToken(validToken));

        // Invalid token should fail validation
        assertFalse(jwtUtil.validateToken("invalid.token.value"));

        // Empty token should fail validation
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void shouldExtractUserIdAndUsername() {
        String token = jwtUtil.generateToken(TEST_USER_ID, TEST_USERNAME);

        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);

        assertEquals(TEST_USER_ID, userId);
        assertEquals(TEST_USERNAME, username);
    }
}
