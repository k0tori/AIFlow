package com.aiflow.auth.controller;

import com.aiflow.auth.dto.LoginRequest;
import com.aiflow.auth.dto.LoginResponse;
import com.aiflow.auth.service.AuthService;
import com.aiflow.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<Result<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> Result.success(authService.login(request)));
    }
}
