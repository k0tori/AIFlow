package com.aiflow.auth.service;

import com.aiflow.auth.dto.LoginRequest;
import com.aiflow.auth.dto.LoginResponse;
import com.aiflow.common.exception.BusinessException;
import com.aiflow.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private LoginRequest validRequest;
    private LoginRequest invalidRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LoginRequest();
        validRequest.setUsername("admin");
        validRequest.setPassword("123456");

        invalidRequest = new LoginRequest();
        invalidRequest.setUsername("admin");
        invalidRequest.setPassword("wrongpassword");
    }

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzI1NiJ9.test.token";
        when(jwtUtil.generateToken(anyLong(), anyString())).thenReturn(expectedToken);

        // Act
        LoginResponse response = authService.login(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("admin", response.getUsername());
    }

    @Test
    void shouldThrowExceptionForInvalidCredentials() {
        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(invalidRequest)
        );

        assertEquals(401, exception.getCode());
        assertEquals("Invalid username or password", exception.getMessage());
    }
}
