package com.aiflow.chat.controller;

import com.aiflow.chat.entity.ChatSession;
import com.aiflow.chat.service.SessionService;
import com.aiflow.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public Mono<Result<ChatSession>> createSession(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String title) {
        return Mono.fromCallable(() -> Result.success(sessionService.createSession(userId, title)));
    }

    @GetMapping
    public Mono<Result<List<ChatSession>>> listSessions(@AuthenticationPrincipal Long userId) {
        return Mono.fromCallable(() -> Result.success(sessionService.listSessions(userId)));
    }

    @GetMapping("/{sessionId}")
    public Mono<Result<ChatSession>> getSession(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId) {
        return Mono.fromCallable(() -> Result.success(sessionService.getSession(sessionId, userId)));
    }

    @PutMapping("/{sessionId}")
    public Mono<Result<ChatSession>> updateSession(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @RequestParam String title) {
        return Mono.fromCallable(() -> Result.success(sessionService.updateSession(sessionId, userId, title)));
    }

    @DeleteMapping("/{sessionId}")
    public Mono<Result<Void>> deleteSession(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId) {
        return Mono.fromRunnable(() -> sessionService.deleteSession(sessionId, userId))
                .then(Mono.just(Result.success()));
    }
}
