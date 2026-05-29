package com.aiflow.auth.service;

import com.aiflow.auth.dto.LoginRequest;
import com.aiflow.auth.dto.LoginResponse;
import com.aiflow.common.exception.BusinessException;
import com.aiflow.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    /**
     * Authenticate user with hardcoded credentials (temporary).
     *
     * @param request the login request containing username and password
     * @return the login response with JWT token
     * @throws BusinessException if credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        // Temporary hardcoded check: admin/123456 -> userId=1
        if ("admin".equals(request.getUsername()) && "123456".equals(request.getPassword())) {
            String token = jwtUtil.generateToken(1L, "admin");
            return LoginResponse.builder()
                    .token(token)
                    .userId(1L)
                    .username("admin")
                    .build();
        }

        throw new BusinessException(401, "Invalid username or password");
    }
}
