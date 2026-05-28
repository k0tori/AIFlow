# AIFlow 智能知识库 Agent 平台实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个基于 Spring AI + RAG + pgvector 的智能知识库 Agent 平台，支持文档上传、向量化、知识库问答、SSE 流式输出和 Tool Calling Agent。

**Architecture:** 单体模块化架构，使用包结构分层（common/auth/chat/rag/agent/file/mapper），通过 Spring AI 集成 DeepSeek Chat 和 BGE-M3 Embedding，使用 pgvector 实现向量存储与检索，RabbitMQ 异步处理文档向量化。

**Tech Stack:** Java 21, Spring Boot 3.4.x, Spring AI 1.0.x, WebFlux, MyBatis-Plus, PostgreSQL, pgvector, Redis, RabbitMQ, Flyway, Docker Compose

---

## 文件结构

```
aiflow-platform/
├── pom.xml
├── docker-compose.yml
├── .gitignore
├── .env.example
└── src/
    ├── main/
    │   ├── java/com/aiflow/
    │   │   ├── AiflowApplication.java
    │   │   ├── common/
    │   │   │   ├── config/
    │   │   │   │   ├── RedisConfig.java
    │   │   │   │   ├── WebFluxConfig.java
    │   │   │   │   ├── JacksonConfig.java
    │   │   │   │   └── MybatisPlusConfig.java
    │   │   │   ├── exception/
    │   │   │   │   ├── BusinessException.java
    │   │   │   │   └── GlobalExceptionHandler.java
    │   │   │   ├── result/
    │   │   │   │   └── Result.java
    │   │   │   ├── util/
    │   │   │   │   └── JwtUtil.java
    │   │   │   └── constant/
    │   │   │       └── SecurityConstants.java
    │   │   ├── auth/
    │   │   │   ├── controller/
    │   │   │   │   └── AuthController.java
    │   │   │   ├── service/
    │   │   │   │   └── AuthService.java
    │   │   │   ├── filter/
    │   │   │   │   └── JwtAuthenticationFilter.java
    │   │   │   ├── config/
    │   │   │   │   └── SecurityConfig.java
    │   │   │   └── dto/
    │   │   │       ├── LoginRequest.java
    │   │   │       └── LoginResponse.java
    │   │   ├── chat/
    │   │   │   ├── controller/
    │   │   │   │   └── ChatController.java
    │   │   │   ├── service/
    │   │   │   │   ├── ChatService.java
    │   │   │   │   └── TokenUsageService.java
    │   │   │   ├── memory/
    │   │   │   │   └── ChatMemoryService.java
    │   │   │   ├── dto/
    │   │   │   │   ├── ChatRequest.java
    │   │   │   │   └── ChatResponse.java
    │   │   │   └── entity/
    │   │   │       ├── ChatMessage.java
    │   │   │       └── ChatUsage.java
    │   │   ├── rag/
    │   │   │   ├── service/
    │   │   │   │   ├── RagService.java
    │   │   │   │   └── EmbeddingService.java
    │   │   │   ├── retrieval/
    │   │   │   │   └── SearchService.java
    │   │   │   ├── parser/
    │   │   │   │   ├── DocumentParser.java
    │   │   │   │   ├── PdfParser.java
    │   │   │   │   ├── MarkdownParser.java
    │   │   │   │   ├── TxtParser.java
    │   │   │   │   └── DocxParser.java
    │   │   │   ├── chunk/
    │   │   │   │   └── ChunkSplitter.java
    │   │   │   ├── strategy/
    │   │   │   │   └── ChunkStrategy.java
    │   │   │   └── entity/
    │   │   │       ├── KnowledgeDocument.java
    │   │   │       └── KnowledgeChunk.java
    │   │   ├── agent/
    │   │   │   ├── service/
    │   │   │   │   └── AgentService.java
    │   │   │   ├── tool/
    │   │   │   │   ├── WeatherTool.java
    │   │   │   │   └── CourseSearchTool.java
    │   │   │   └── prompt/
    │   │   │       └── AgentPromptProvider.java
    │   │   ├── file/
    │   │   │   ├── controller/
    │   │   │   │   └── FileController.java
    │   │   │   ├── service/
    │   │   │   │   └── FileService.java
    │   │   │   ├── consumer/
    │   │   │   │   └── FileConsumer.java
    │   │   │   └── entity/
    │   │   │       └── FileMetadata.java
    │   │   └── mapper/
    │   │       ├── KnowledgeDocumentMapper.java
    │   │       ├── KnowledgeChunkMapper.java
    │   │       └── ChatUsageMapper.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── prompts/
    │       │   ├── system/
    │       │   │   └── system-prompt.txt
    │       │   ├── rag/
    │       │   │   └── rag-prompt.txt
    │       │   └── tool/
    │       │       └── tool-agent-prompt.txt
    │       ├── mapper/
    │       │   └── KnowledgeChunkMapper.xml
    │       └── db/
    │           └── migration/
    │               ├── V1__init_schema.sql
    │               └── V2__add_chat_usage.sql
    └── test/
        └── java/com/aiflow/
            ├── common/
            │   └── util/
            │       └── JwtUtilTest.java
            ├── auth/
            │   └── service/
            │       └── AuthServiceTest.java
            ├── chat/
            │   └── service/
            │               └── ChatServiceTest.java
            └── rag/
                ├── service/
                │   └── EmbeddingServiceTest.java
                └── chunk/
                    └── ChunkSplitterTest.java
```

---

## 第一阶段：基础框架 + AI 对话

### Task 1: 项目初始化

**Files:**
- Create: `pom.xml`
- Create: `.gitignore`
- Create: `src/main/java/com/aiflow/AiflowApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-dev.yml`

- [ ] **Step 1: 创建父 POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>com.aiflow</groupId>
    <artifactId>aiflow-platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>aiflow-platform</name>
    <description>AIFlow - AI Native Knowledge Base Agent Platform</description>

    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <knife4j.version>4.5.0</knife4j.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <jjwt.version>0.12.6</jjwt.version>
        <poi.version>5.2.5</poi.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring AI -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.pgvector</groupId>
            <artifactId>pgvector</artifactId>
            <version>0.1.6</version>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Document Parsing -->
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>3.0.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>${poi.version}</version>
        </dependency>

        <!-- Knife4j -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建 .gitignore**

```
# IDE
.idea/
*.iml
.vscode/

# Build
target/
*.class
*.jar
*.war

# Logs
*.log
logs/

# Env
.env
.env.local

# OS
.DS_Store
Thumbs.db
```

- [ ] **Step 3: 创建启动类**

```java
package com.aiflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiflowApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
spring:
  profiles:
    active: dev
  application:
    name: aiflow-platform
```

- [ ] **Step 5: 创建 application-dev.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:15432/aiflow
    username: aiflow
    password: ${DB_PASSWORD:123456}
    driver-class-name: org.postgresql.Driver

  data:
    redis:
      host: localhost
      port: 16379
      password: ${REDIS_PASSWORD:}

  rabbitmq:
    host: localhost
    port: 15672
    username: aiflow
    password: ${MQ_PASSWORD:123456}

  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
          temperature: 0.7
      embedding:
        options:
          model: deepseek-embedding

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

  flyway:
    enabled: true
    locations: classpath:db/migration

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto

knife4j:
  enable: true
  setting:
    language: zh_cn

logging:
  level:
    com.aiflow: DEBUG
    org.springframework.ai: DEBUG
```

- [ ] **Step 6: 提交**

```bash
git add pom.xml .gitignore src/
git commit -m "feat: initialize project structure with Spring Boot 3.4.x"
```

---

### Task 2: 基础配置类（aiflow-common）

**Files:**
- Create: `src/main/java/com/aiflow/common/result/Result.java`
- Create: `src/main/java/com/aiflow/common/exception/BusinessException.java`
- Create: `src/main/java/com/aiflow/common/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/com/aiflow/common/config/RedisConfig.java`
- Create: `src/main/java/com/aiflow/common/config/WebFluxConfig.java`
- Create: `src/main/java/com/aiflow/common/config/JacksonConfig.java`
- Create: `src/main/java/com/aiflow/common/config/MybatisPlusConfig.java`

- [ ] **Step 1: 创建 Result 统一返回**

```java
package com.aiflow.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
```

- [ ] **Step 2: 创建 BusinessException**

```java
package com.aiflow.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **Step 3: 创建 GlobalExceptionHandler**

```java
package com.aiflow.common.exception;

import com.aiflow.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.error("Internal server error");
    }
}
```

- [ ] **Step 4: 创建 RedisConfig**

```java
package com.aiflow.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```

- [ ] **Step 5: 创建 WebFluxConfig**

```java
package com.aiflow.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

- [ ] **Step 6: 创建 JacksonConfig**

```java
package com.aiflow.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
```

- [ ] **Step 7: 创建 MybatisPlusConfig**

```java
package com.aiflow.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}
```

- [ ] **Step 8: 提交**

```bash
git add src/main/java/com/aiflow/common/
git commit -m "feat: add common module with Result, exception handling, and configs"
```

---

### Task 3: JWT 工具类

**Files:**
- Create: `src/main/java/com/aiflow/common/util/JwtUtil.java`
- Create: `src/main/java/com/aiflow/common/constant/SecurityConstants.java`
- Create: `src/test/java/com/aiflow/common/util/JwtUtilTest.java`

- [ ] **Step 1: 创建 SecurityConstants**

```java
package com.aiflow.common.constant;

public class SecurityConstants {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    public static final long TOKEN_EXPIRATION = 24 * 60 * 60 * 1000; // 24 hours
}
```

- [ ] **Step 2: 创建 JwtUtil**

```java
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

@Component
public class JwtUtil {

    @Value("${jwt.secret:aiflow-platform-secret-key-must-be-at-least-256-bits}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + SecurityConstants.TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
}
```

- [ ] **Step 3: 创建 JwtUtilTest**

```java
package com.aiflow.common.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void shouldGenerateAndParseToken() {
        Long userId = 1L;
        String username = "testuser";

        String token = jwtUtil.generateToken(userId, username);
        assertNotNull(token);

        Claims claims = jwtUtil.parseToken(token);
        assertEquals(String.valueOf(userId), claims.getSubject());
        assertEquals(username, claims.get("username", String.class));
    }

    @Test
    void shouldValidateToken() {
        String token = jwtUtil.generateToken(1L, "testuser");
        assertTrue(jwtUtil.validateToken(token));
        assertFalse(jwtUtil.validateToken("invalid-token"));
    }

    @Test
    void shouldExtractUserIdAndUsername() {
        Long userId = 1L;
        String username = "testuser";
        String token = jwtUtil.generateToken(userId, username);

        assertEquals(userId, jwtUtil.getUserId(token));
        assertEquals(username, jwtUtil.getUsername(token));
    }
}
```

- [ ] **Step 4: 运行测试**

```bash
mvn test -Dtest=JwtUtilTest -pl .
```

Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/aiflow/common/ src/test/java/com/aiflow/common/
git commit -m "feat: add JWT utility for authentication"
```

---

### Task 4: 认证模块（aiflow-auth）

**Files:**
- Create: `src/main/java/com/aiflow/auth/dto/LoginRequest.java`
- Create: `src/main/java/com/aiflow/auth/dto/LoginResponse.java`
- Create: `src/main/java/com/aiflow/auth/service/AuthService.java`
- Create: `src/main/java/com/aiflow/auth/controller/AuthController.java`
- Create: `src/main/java/com/aiflow/auth/filter/JwtAuthenticationFilter.java`
- Create: `src/main/java/com/aiflow/auth/config/SecurityConfig.java`
- Create: `src/test/java/com/aiflow/auth/service/AuthServiceTest.java`

- [ ] **Step 1: 创建 LoginRequest**

```java
package com.aiflow.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
```

- [ ] **Step 2: 创建 LoginResponse**

```java
package com.aiflow.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
}
```

- [ ] **Step 3: 创建 AuthService**

```java
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

    // TODO: Replace with database user lookup
    public LoginResponse login(LoginRequest request) {
        // Temporary hardcoded user for testing
        if ("admin".equals(request.getUsername()) && "123456".equals(request.getPassword())) {
            String token = jwtUtil.generateToken(1L, request.getUsername());
            return LoginResponse.builder()
                    .token(token)
                    .userId(1L)
                    .username(request.getUsername())
                    .build();
        }
        throw new BusinessException(401, "Invalid username or password");
    }
}
```

- [ ] **Step 4: 创建 AuthController**

```java
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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
}
```

- [ ] **Step 5: 创建 JwtAuthenticationFilter**

```java
package com.aiflow.auth.filter;

import com.aiflow.common.constant.SecurityConstants;
import com.aiflow.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_NAME);

        if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        return chain.filter(exchange);
    }
}
```

- [ ] **Step 6: 创建 SecurityConfig**

```java
package com.aiflow.auth.config;

import com.aiflow.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/login", "/auth/register").permitAll()
                        .pathMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 7: 创建 AuthServiceTest**

```java
package com.aiflow.auth.service;

import com.aiflow.auth.dto.LoginRequest;
import com.aiflow.auth.dto.LoginResponse;
import com.aiflow.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        LoginResponse response = authService.login(request);

        assertNotNull(response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("admin", response.getUsername());
    }

    @Test
    void shouldThrowExceptionForInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
```

- [ ] **Step 8: 运行测试**

```bash
mvn test -Dtest=AuthServiceTest -pl .
```

Expected: PASS

- [ ] **Step 9: 提交**

```bash
git add src/main/java/com/aiflow/auth/ src/test/java/com/aiflow/auth/
git commit -m "feat: add auth module with JWT authentication"
```

---

### Task 5: Docker Compose 基础设施

**Files:**
- Create: `docker-compose.yml`
- Create: `.env.example`

- [ ] **Step 1: 创建 docker-compose.yml**

```yaml
services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: aiflow-postgres
    environment:
      POSTGRES_DB: aiflow
      POSTGRES_USER: aiflow
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "15432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U aiflow"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: aiflow-redis
    ports:
      - "16379:6379"
    volumes:
      - redisdata:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: aiflow-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: aiflow
      RABBITMQ_DEFAULT_PASS: ${MQ_PASSWORD}
    ports:
      - "15672:5672"
      - "25672:15672"
    volumes:
      - mqdata:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_running"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  pgdata:
  redisdata:
  mqdata:
```

- [ ] **Step 2: 创建 .env.example**

```env
# Database
DB_PASSWORD=123456

# Redis
REDIS_PASSWORD=

# RabbitMQ
MQ_PASSWORD=123456

# DeepSeek API
DEEPSEEK_API_KEY=your-api-key-here

# JWT
JWT_SECRET=aiflow-platform-secret-key-must-be-at-least-256-bits
```

- [ ] **Step 3: 启动基础设施**

```bash
docker compose up -d
```

- [ ] **Step 4: 验证服务状态**

```bash
docker compose ps
```

Expected: All services are running and healthy

- [ ] **Step 5: 提交**

```bash
git add docker-compose.yml .env.example
git commit -m "feat: add Docker Compose for PostgreSQL, Redis, RabbitMQ"
```

---

### Task 6: Flyway 数据库初始化

**Files:**
- Create: `src/main/resources/db/migration/V1__init_schema.sql`
- Create: `src/main/resources/db/migration/V2__add_chat_usage.sql`

- [ ] **Step 1: 创建 V1__init_schema.sql**

```sql
-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin user (password: 123456)
INSERT INTO users (username, password) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi');

-- Knowledge document table
CREATE TABLE knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Knowledge chunk table with pgvector
CREATE TABLE knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT REFERENCES knowledge_document(id),
    chunk_index INT,
    content TEXT,
    embedding vector(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chat session table
CREATE TABLE chat_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chat message table
CREATE TABLE chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES chat_session(id),
    role VARCHAR(20) NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for vector similarity search
CREATE INDEX idx_chunk_embedding ON knowledge_chunk USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Create indexes for foreign keys
CREATE INDEX idx_chunk_document_id ON knowledge_chunk(document_id);
CREATE INDEX idx_message_session_id ON chat_message(session_id);
```

- [ ] **Step 2: 创建 V2__add_chat_usage.sql**

```sql
-- Chat usage table for token tracking
CREATE TABLE chat_usage (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES chat_session(id),
    model_name VARCHAR(100),
    prompt_tokens INT,
    completion_tokens INT,
    total_tokens INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usage_session_id ON chat_usage(session_id);
```

- [ ] **Step 3: 运行 Flyway 迁移**

```bash
mvn flyway:migrate
```

Expected: Migration successful

- [ ] **Step 4: 提交**

```bash
git add src/main/resources/db/migration/
git commit -m "feat: add Flyway database migrations"
```

---

### Task 7: Prompt 模板

**Files:**
- Create: `src/main/resources/prompts/system/system-prompt.txt`
- Create: `src/main/resources/prompts/rag/rag-prompt.txt`
- Create: `src/main/resources/prompts/tool/tool-agent-prompt.txt`

- [ ] **Step 1: 创建 system-prompt.txt**

```
你是企业知识库 AI 助手。

请严格基于提供的上下文回答问题。

如果上下文不存在答案，请明确说明不知道。

禁止编造内容。

回答要简洁、专业、有帮助。
```

- [ ] **Step 2: 创建 rag-prompt.txt**

```
基于以下上下文回答用户问题。

上下文：
{context}

用户问题：
{question}

如果上下文中没有相关信息，请说明无法根据现有知识回答。
```

- [ ] **Step 3: 创建 tool-agent-prompt.txt**

```
你是一个智能助手，可以使用工具来帮助用户。

可用工具：
- getWeather: 获取指定城市的天气信息
- queryCourse: 根据关键词搜索课程

请根据用户问题判断是否需要调用工具。如果需要，请调用相应的工具获取信息。
```

- [ ] **Step 4: 提交**

```bash
git add src/main/resources/prompts/
git commit -m "feat: add prompt templates"
```

---

### Task 8: AI 对话模块（aiflow-chat）

**Files:**
- Create: `src/main/java/com/aiflow/chat/dto/ChatRequest.java`
- Create: `src/main/java/com/aiflow/chat/dto/ChatResponse.java`
- Create: `src/main/java/com/aiflow/chat/entity/ChatMessage.java`
- Create: `src/main/java/com/aiflow/chat/entity/ChatUsage.java`
- Create: `src/main/java/com/aiflow/chat/memory/ChatMemoryService.java`
- Create: `src/main/java/com/aiflow/chat/service/ChatService.java`
- Create: `src/main/java/com/aiflow/chat/service/TokenUsageService.java`
- Create: `src/main/java/com/aiflow/chat/controller/ChatController.java`
- Create: `src/main/java/com/aiflow/mapper/ChatUsageMapper.java`
- Create: `src/test/java/com/aiflow/chat/service/ChatServiceTest.java`

- [ ] **Step 1: 创建 ChatRequest**

```java
package com.aiflow.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotBlank(message = "Message is required")
    private String message;
}
```

- [ ] **Step 2: 创建 ChatResponse**

```java
package com.aiflow.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private String content;
    private String role;
}
```

- [ ] **Step 3: 创建 ChatMessage**

```java
package com.aiflow.chat.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: 创建 ChatUsage**

```java
package com.aiflow.chat.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatUsage {
    private Long id;
    private Long sessionId;
    private String modelName;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 5: 创建 ChatMemoryService**

```java
package com.aiflow.chat.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "chat:memory:";
    private static final int MAX_MESSAGES = 20; // 10 rounds
    private static final long EXPIRE_HOURS = 24;

    public List<String> getHistory(Long sessionId) {
        String key = KEY_PREFIX + sessionId;
        List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .map(Object::toString)
                .toList();
    }

    public void addMessage(Long sessionId, String role, String content) {
        String key = KEY_PREFIX + sessionId;
        String message = role + ":" + content;
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
        redisTemplate.expire(key, EXPIRE_HOURS, TimeUnit.HOURS);
    }

    public void clearHistory(Long sessionId) {
        String key = KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}
```

- [ ] **Step 6: 创建 TokenUsageService**

```java
package com.aiflow.chat.service;

import com.aiflow.chat.entity.ChatUsage;
import com.aiflow.mapper.ChatUsageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenUsageService {

    private final ChatUsageMapper chatUsageMapper;

    public void saveUsage(Long sessionId, String modelName, int promptTokens, int completionTokens) {
        ChatUsage usage = new ChatUsage();
        usage.setSessionId(sessionId);
        usage.setModelName(modelName);
        usage.setPromptTokens(promptTokens);
        usage.setCompletionTokens(completionTokens);
        usage.setTotalTokens(promptTokens + completionTokens);
        chatUsageMapper.insert(usage);
    }
}
```

- [ ] **Step 7: 创建 ChatService**

```java
package com.aiflow.chat.service;

import com.aiflow.chat.dto.ChatRequest;
import com.aiflow.chat.memory.ChatMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemoryService chatMemoryService;
    private final TokenUsageService tokenUsageService;

    private final Resource systemPrompt = new ClassPathResource("prompts/system/system-prompt.txt");

    public Flux<String> streamChat(ChatRequest request) {
        ChatClient chatClient = chatClientBuilder.build();

        // Load history
        List<String> history = chatMemoryService.getHistory(request.getSessionId());

        // Build prompt
        String system = readResource(systemPrompt);
        String historyText = history.stream()
                .map(h -> {
                    String[] parts = h.split(":", 2);
                    return parts.length == 2 ? parts[0] + ": " + parts[1] : h;
                })
                .collect(Collectors.joining("\n"));

        String fullPrompt = system + "\n\n" + historyText + "\n\nUser: " + request.getMessage();

        // Save user message
        chatMemoryService.addMessage(request.getSessionId(), "User", request.getMessage());

        // Stream response
        return chatClient.prompt()
                .user(fullPrompt)
                .stream()
                .content()
                .doOnNext(chunk -> {
                    // Save complete response when done
                })
                .doOnComplete(() -> {
                    // Save assistant message
                    chatMemoryService.addMessage(request.getSessionId(), "Assistant", "...");
                });
    }

    private String readResource(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read prompt resource", e);
            return "";
        }
    }
}
```

- [ ] **Step 8: 创建 ChatController**

```java
package com.aiflow.chat.controller;

import com.aiflow.chat.dto.ChatRequest;
import com.aiflow.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatRequest request) {
        return chatService.streamChat(request);
    }
}
```

- [ ] **Step 9: 创建 ChatUsageMapper**

```java
package com.aiflow.mapper;

import com.aiflow.chat.entity.ChatUsage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatUsageMapper extends BaseMapper<ChatUsage> {
}
```

- [ ] **Step 10: 创建 ChatServiceTest**

```java
package com.aiflow.chat.service;

import com.aiflow.chat.dto.ChatRequest;
import com.aiflow.chat.memory.ChatMemoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMemoryService chatMemoryService;

    @Test
    void shouldStreamChat() {
        ChatRequest request = new ChatRequest();
        request.setSessionId(1L);
        request.setMessage("Hello, how are you?");

        Flux<String> response = chatService.streamChat(request);

        StepVerifier.create(response)
                .expectNextCount(1)
                .verifyComplete();
    }
}
```

- [ ] **Step 11: 运行测试**

```bash
mvn test -Dtest=ChatServiceTest -pl .
```

Expected: PASS

- [ ] **Step 12: 提交**

```bash
git add src/main/java/com/aiflow/chat/ src/main/java/com/aiflow/mapper/ src/test/java/com/aiflow/chat/
git commit -m "feat: add chat module with SSE streaming and Redis memory"
```

---

## 第二阶段：文件上传 + 文档解析

### Task 9: 实体类与 Mapper

**Files:**
- Create: `src/main/java/com/aiflow/rag/entity/KnowledgeDocument.java`
- Create: `src/main/java/com/aiflow/rag/entity/KnowledgeChunk.java`
- Create: `src/main/java/com/aiflow/mapper/KnowledgeDocumentMapper.java`
- Create: `src/main/java/com/aiflow/mapper/KnowledgeChunkMapper.java`
- Create: `src/main/resources/mapper/KnowledgeChunkMapper.xml`

- [ ] **Step 1: 创建 KnowledgeDocument**

```java
package com.aiflow.rag.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeDocument {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 KnowledgeChunk**

```java
package com.aiflow.rag.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeChunk {
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String content;
    private float[] embedding;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: 创建 KnowledgeDocumentMapper**

```java
package com.aiflow.mapper;

import com.aiflow.rag.entity.KnowledgeDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
}
```

- [ ] **Step 4: 创建 KnowledgeChunkMapper**

```java
package com.aiflow.mapper;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {

    @Select("SELECT id, document_id, chunk_index, content, " +
            "1 - (embedding <=> #{vector}::vector) AS similarity " +
            "FROM knowledge_chunk " +
            "ORDER BY embedding <=> #{vector}::vector " +
            "LIMIT #{topK}")
    List<KnowledgeChunk> similaritySearch(@Param("vector") String vector, @Param("topK") int topK);
}
```

- [ ] **Step 5: 创建 KnowledgeChunkMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.aiflow.mapper.KnowledgeChunkMapper">

    <insert id="insertBatch" parameterType="list">
        INSERT INTO knowledge_chunk (document_id, chunk_index, content, embedding)
        VALUES
        <foreach collection="list" item="chunk" separator=",">
            (#{chunk.documentId}, #{chunk.chunkIndex}, #{chunk.content}, #{chunk.embedding}::vector)
        </foreach>
    </insert>

</mapper>
```

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/aiflow/rag/entity/ src/main/java/com/aiflow/mapper/ src/main/resources/mapper/
git commit -m "feat: add entity classes and mappers for knowledge base"
```

---

### Task 10: 文档解析器

**Files:**
- Create: `src/main/java/com/aiflow/rag/parser/DocumentParser.java`
- Create: `src/main/java/com/aiflow/rag/parser/PdfParser.java`
- Create: `src/main/java/com/aiflow/rag/parser/MarkdownParser.java`
- Create: `src/main/java/com/aiflow/rag/parser/TxtParser.java`
- Create: `src/main/java/com/aiflow/rag/parser/DocxParser.java`

- [ ] **Step 1: 创建 DocumentParser 接口**

```java
package com.aiflow.rag.parser;

import java.io.InputStream;

public interface DocumentParser {
    String parse(InputStream inputStream) throws Exception;
    String supportsType();
}
```

- [ ] **Step 2: 创建 PdfParser**

```java
package com.aiflow.rag.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class PdfParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    @Override
    public String supportsType() {
        return "pdf";
    }
}
```

- [ ] **Step 3: 创建 MarkdownParser**

```java
package com.aiflow.rag.parser;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class MarkdownParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        // Remove markdown syntax for plain text
        return content
                .replaceAll("#+ ", "")
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\*(.*?)\\*", "$1")
                .replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1")
                .replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("`([^`]+)`", "$1");
    }

    @Override
    public String supportsType() {
        return "md";
    }
}
```

- [ ] **Step 4: 创建 TxtParser**

```java
package com.aiflow.rag.parser;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TxtParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public String supportsType() {
        return "txt";
    }
}
```

- [ ] **Step 5: 创建 DocxParser**

```java
package com.aiflow.rag.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.stream.Collectors;

@Component
public class DocxParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            return document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    @Override
    public String supportsType() {
        return "docx";
    }
}
```

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/aiflow/rag/parser/
git commit -m "feat: add document parsers for PDF, Markdown, TXT, DOCX"
```

---

### Task 11: Chunk 切片器

**Files:**
- Create: `src/main/java/com/aiflow/rag/chunk/ChunkSplitter.java`
- Create: `src/main/java/com/aiflow/rag/strategy/ChunkStrategy.java`
- Create: `src/test/java/com/aiflow/rag/chunk/ChunkSplitterTest.java`

- [ ] **Step 1: 创建 ChunkStrategy**

```java
package com.aiflow.rag.strategy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkStrategy {
    private int chunkSize;
    private int overlap;

    public static ChunkStrategy defaultStrategy() {
        return ChunkStrategy.builder()
                .chunkSize(500)
                .overlap(100)
                .build();
    }
}
```

- [ ] **Step 2: 创建 ChunkSplitter**

```java
package com.aiflow.rag.chunk;

import com.aiflow.rag.strategy.ChunkStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChunkSplitter {

    public List<String> split(String text, ChunkStrategy strategy) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + strategy.getChunkSize(), length);

            // Try to find a sentence boundary
            if (end < length) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int boundary = Math.max(lastPeriod, lastNewline);

                if (boundary > start + strategy.getChunkSize() / 2) {
                    end = boundary + 1;
                }
            }

            chunks.add(text.substring(start, end).trim());
            start = end - strategy.getOverlap();
        }

        return chunks.stream()
                .filter(chunk -> !chunk.isEmpty())
                .toList();
    }

    public List<String> split(String text) {
        return split(text, ChunkStrategy.defaultStrategy());
    }
}
```

- [ ] **Step 3: 创建 ChunkSplitterTest**

```java
package com.aiflow.rag.chunk;

import com.aiflow.rag.strategy.ChunkStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChunkSplitterTest {

    @Autowired
    private ChunkSplitter chunkSplitter;

    @Test
    void shouldSplitText() {
        String text = "This is a test document. It contains multiple sentences. " +
                "Each sentence has different length. The splitter should handle them properly.";

        List<String> chunks = chunkSplitter.split(text, ChunkStrategy.builder()
                .chunkSize(50)
                .overlap(10)
                .build());

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.size() > 1);
    }

    @Test
    void shouldUseDefaultStrategy() {
        String text = "A".repeat(1000);
        List<String> chunks = chunkSplitter.split(text);

        assertFalse(chunks.isEmpty());
    }
}
```

- [ ] **Step 4: 运行测试**

```bash
mvn test -Dtest=ChunkSplitterTest -pl .
```

Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/aiflow/rag/chunk/ src/main/java/com/aiflow/rag/strategy/ src/test/java/com/aiflow/rag/chunk/
git commit -m "feat: add chunk splitter for document processing"
```

---

### Task 12: 文件上传模块（aiflow-file）

**Files:**
- Create: `src/main/java/com/aiflow/file/entity/FileMetadata.java`
- Create: `src/main/java/com/aiflow/file/service/FileService.java`
- Create: `src/main/java/com/aiflow/file/controller/FileController.java`
- Create: `src/main/java/com/aiflow/file/consumer/FileConsumer.java`

- [ ] **Step 1: 创建 FileMetadata**

```java
package com.aiflow.file.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileMetadata {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 FileService**

```java
package com.aiflow.file.service;

import com.aiflow.rag.entity.KnowledgeDocument;
import com.aiflow.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final KnowledgeDocumentMapper documentMapper;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "rag.document.exchange";
    private static final String ROUTING_KEY = "rag.document.parse";

    public KnowledgeDocument uploadFile(MultipartFile file) throws IOException {
        // Validate file type
        String fileType = getFileType(file.getOriginalFilename());
        if (!isSupportedType(fileType)) {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }

        // Save document metadata
        KnowledgeDocument document = new KnowledgeDocument();
        document.setFileName(file.getOriginalFilename());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setStatus("PENDING");
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        documentMapper.insert(document);

        // Send MQ message for async processing
        Map<String, Object> message = Map.of(
                "documentId", document.getId(),
                "fileName", file.getOriginalFilename(),
                "fileType", fileType,
                "filePath", saveFile(file)
        );
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);

        log.info("File uploaded: {}, documentId: {}", file.getOriginalFilename(), document.getId());
        return document;
    }

    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isSupportedType(String fileType) {
        return fileType.equals("pdf") || fileType.equals("md") ||
                fileType.equals("txt") || fileType.equals("docx");
    }

    private String saveFile(MultipartFile file) throws IOException {
        // TODO: Implement file storage (local or S3)
        // For now, return a placeholder path
        return "/tmp/aiflow/" + file.getOriginalFilename();
    }
}
```

- [ ] **Step 3: 创建 FileController**

```java
package com.aiflow.file.controller;

import com.aiflow.common.result.Result;
import com.aiflow.file.service.FileService;
import com.aiflow.rag.entity.KnowledgeDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<KnowledgeDocument> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return Result.success(fileService.uploadFile(file));
        } catch (Exception e) {
            return Result.error("Upload failed: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 4: 创建 FileConsumer**

```java
package com.aiflow.file.consumer;

import com.aiflow.rag.chunk.ChunkSplitter;
import com.aiflow.rag.entity.KnowledgeChunk;
import com.aiflow.rag.entity.KnowledgeDocument;
import com.aiflow.rag.parser.DocumentParser;
import com.aiflow.mapper.KnowledgeChunkMapper;
import com.aiflow.mapper.KnowledgeDocumentMapper;
import com.aiflow.rag.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileConsumer {

    private final List<DocumentParser> parsers;
    private final ChunkSplitter chunkSplitter;
    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;

    @RabbitListener(queues = "rag.document.parse.queue")
    public void handleParseMessage(Map<String, Object> message) {
        Long documentId = ((Number) message.get("documentId")).longValue();
        String fileType = (String) message.get("fileType");
        String filePath = (String) message.get("filePath");

        log.info("Processing document: {}", documentId);

        try {
            // Update status to PROCESSING
            KnowledgeDocument document = documentMapper.selectById(documentId);
            document.setStatus("PROCESSING");
            documentMapper.updateById(document);

            // Parse document
            DocumentParser parser = parsers.stream()
                    .filter(p -> p.supportsType().equals(fileType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No parser for type: " + fileType));

            String content = parser.parse(new FileInputStream(filePath));

            // Split into chunks
            List<String> chunks = chunkSplitter.split(content);

            // Generate embeddings and save
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                float[] embedding = embeddingService.embed(chunk);

                KnowledgeChunk knowledgeChunk = new KnowledgeChunk();
                knowledgeChunk.setDocumentId(documentId);
                knowledgeChunk.setChunkIndex(i);
                knowledgeChunk.setContent(chunk);
                knowledgeChunk.setEmbedding(embedding);
                chunkMapper.insert(knowledgeChunk);
            }

            // Update status to COMPLETED
            document.setStatus("COMPLETED");
            documentMapper.updateById(document);

            log.info("Document processed: {}, chunks: {}", documentId, chunks.size());
        } catch (Exception e) {
            log.error("Failed to process document: {}", documentId, e);

            // Update status to FAILED
            KnowledgeDocument document = documentMapper.selectById(documentId);
            document.setStatus("FAILED");
            documentMapper.updateById(document);
        }
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/aiflow/file/
git commit -m "feat: add file upload module with async processing"
```

---

## 第三阶段：向量检索 + RAG

### Task 13: Embedding 服务

**Files:**
- Create: `src/main/java/com/aiflow/rag/service/EmbeddingService.java`
- Create: `src/test/java/com/aiflow/rag/service/EmbeddingServiceTest.java`

- [ ] **Step 1: 创建 EmbeddingService**

```java
package com.aiflow.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingClient embeddingClient;

    public float[] embed(String text) {
        try {
            List<float[]> embeddings = embeddingClient.embed(List.of(text));
            return embeddings.get(0);
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            throw new RuntimeException("Embedding failed", e);
        }
    }

    public List<float[]> embedBatch(List<String> texts) {
        try {
            return embeddingClient.embed(texts);
        } catch (Exception e) {
            log.error("Failed to generate batch embeddings", e);
            throw new RuntimeException("Batch embedding failed", e);
        }
    }
}
```

- [ ] **Step 2: 创建 EmbeddingServiceTest**

```java
package com.aiflow.rag.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmbeddingServiceTest {

    @Autowired
    private EmbeddingService embeddingService;

    @Test
    void shouldGenerateEmbedding() {
        String text = "Hello, this is a test sentence.";

        float[] embedding = embeddingService.embed(text);

        assertNotNull(embedding);
        assertEquals(1024, embedding.length);
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
mvn test -Dtest=EmbeddingServiceTest -pl .
```

Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/aiflow/rag/service/ src/test/java/com/aiflow/rag/service/
git commit -m "feat: add embedding service using DeepSeek API"
```

---

### Task 14: 检索服务

**Files:**
- Create: `src/main/java/com/aiflow/rag/retrieval/SearchService.java`

- [ ] **Step 1: 创建 SearchService**

```java
package com.aiflow.rag.retrieval;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.aiflow.mapper.KnowledgeChunkMapper;
import com.aiflow.rag.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingService embeddingService;

    private static final int DEFAULT_TOP_K = 5;

    public List<KnowledgeChunk> search(String query) {
        return search(query, DEFAULT_TOP_K);
    }

    public List<KnowledgeChunk> search(String query, int topK) {
        // Generate query embedding
        float[] queryEmbedding = embeddingService.embed(query);

        // Convert to pgvector format
        String vectorStr = convertToPgVector(queryEmbedding);

        // Perform similarity search
        return chunkMapper.similaritySearch(vectorStr, topK);
    }

    private String convertToPgVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/aiflow/rag/retrieval/
git commit -m "feat: add search service for vector similarity search"
```

---

### Task 15: RAG 服务

**Files:**
- Create: `src/main/java/com/aiflow/rag/service/RagService.java`

- [ ] **Step 1: 创建 RagService**

```java
package com.aiflow.rag.service;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.aiflow.rag.retrieval.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final SearchService searchService;

    private final Resource ragPrompt = new ClassPathResource("prompts/rag/rag-prompt.txt");

    public String buildRagContext(String question) {
        // Search for relevant chunks
        List<KnowledgeChunk> chunks = searchService.search(question);

        // Build context
        String context = chunks.stream()
                .map(KnowledgeChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        // Load prompt template
        String template = readResource(ragPrompt);

        // Replace placeholders
        return template
                .replace("{context}", context)
                .replace("{question}", question);
    }

    private String readResource(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read RAG prompt", e);
            return "Context:\n{context}\n\nQuestion: {question}";
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/aiflow/rag/service/
git commit -m "feat: add RAG service for context augmentation"
```

---

## 第四阶段：Agent + Token Usage

### Task 16: Tool Calling Agent

**Files:**
- Create: `src/main/java/com/aiflow/agent/tool/WeatherTool.java`
- Create: `src/main/java/com/aiflow/agent/tool/CourseSearchTool.java`
- Create: `src/main/java/com/aiflow/agent/service/AgentService.java`
- Create: `src/main/java/com/aiflow/agent/prompt/AgentPromptProvider.java`

- [ ] **Step 1: 创建 WeatherTool**

```java
package com.aiflow.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {

    @Tool(description = "获取指定城市的天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        // Simulated weather data
        return String.format("%s今天天气晴朗，温度25°C，湿度60%%，微风。", city);
    }
}
```

- [ ] **Step 2: 创建 CourseSearchTool**

```java
package com.aiflow.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CourseSearchTool {

    private static final Map<String, List<String>> COURSES = Map.of(
            "java", List.of("Java基础入门", "Spring Boot实战", "Java并发编程"),
            "python", List.of("Python基础", "Python数据分析", "Django Web开发"),
            "ai", List.of("机器学习基础", "深度学习入门", "NLP自然语言处理")
    );

    @Tool(description = "根据关键词搜索课程")
    public String queryCourse(@ToolParam(description = "搜索关键词") String keyword) {
        List<String> results = COURSES.entrySet().stream()
                .filter(e -> e.getKey().contains(keyword.toLowerCase()) ||
                        keyword.toLowerCase().contains(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .toList();

        if (results.isEmpty()) {
            return "未找到相关课程";
        }

        return "找到以下课程：\n" + String.join("\n", results);
    }
}
```

- [ ] **Step 3: 创建 AgentPromptProvider**

```java
package com.aiflow.agent.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AgentPromptProvider {

    private final Resource agentPrompt = new ClassPathResource("prompts/tool/tool-agent-prompt.txt");

    public String getSystemPrompt() {
        try {
            return agentPrompt.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "你是一个智能助手，可以使用工具来帮助用户。";
        }
    }
}
```

- [ ] **Step 4: 创建 AgentService**

```java
package com.aiflow.agent.service;

import com.aiflow.agent.prompt.AgentPromptProvider;
import com.aiflow.agent.tool.CourseSearchTool;
import com.aiflow.agent.tool.WeatherTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final ChatClient.Builder chatClientBuilder;
    private final WeatherTool weatherTool;
    private final CourseSearchTool courseSearchTool;
    private final AgentPromptProvider promptProvider;

    public Flux<String> streamAgent(String message) {
        ChatClient chatClient = chatClientBuilder
                .defaultTools(weatherTool, courseSearchTool)
                .defaultSystem(promptProvider.getSystemPrompt())
                .build();

        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/aiflow/agent/
git commit -m "feat: add agent module with tool calling support"
```

---

### Task 17: 集成 Agent 到 Chat

**Files:**
- Modify: `src/main/java/com/aiflow/chat/service/ChatService.java`
- Modify: `src/main/java/com/aiflow/chat/controller/ChatController.java`

- [ ] **Step 1: 更新 ChatService 集成 Agent**

```java
package com.aiflow.chat.service;

import com.aiflow.agent.service.AgentService;
import com.aiflow.chat.dto.ChatRequest;
import com.aiflow.chat.memory.ChatMemoryService;
import com.aiflow.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemoryService chatMemoryService;
    private final TokenUsageService tokenUsageService;
    private final RagService ragService;
    private final AgentService agentService;

    private final Resource systemPrompt = new ClassPathResource("prompts/system/system-prompt.txt");

    public Flux<String> streamChat(ChatRequest request) {
        // Load history
        List<String> history = chatMemoryService.getHistory(request.getSessionId());

        // Build RAG context
        String ragContext = ragService.buildRagContext(request.getMessage());

        // Build prompt
        String system = readResource(systemPrompt);
        String historyText = history.stream()
                .map(h -> {
                    String[] parts = h.split(":", 2);
                    return parts.length == 2 ? parts[0] + ": " + parts[1] : h;
                })
                .collect(Collectors.joining("\n"));

        String fullPrompt = system + "\n\n" + ragContext + "\n\n" + historyText + "\n\nUser: " + request.getMessage();

        // Save user message
        chatMemoryService.addMessage(request.getSessionId(), "User", request.getMessage());

        // Stream response
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .user(fullPrompt)
                .stream()
                .content()
                .doOnComplete(() -> {
                    // TODO: Save assistant message and token usage
                });
    }

    public Flux<String> streamAgentChat(ChatRequest request) {
        // Save user message
        chatMemoryService.addMessage(request.getSessionId(), "User", request.getMessage());

        // Use agent with tools
        return agentService.streamAgent(request.getMessage())
                .doOnComplete(() -> {
                    // TODO: Save assistant message
                });
    }

    private String readResource(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read prompt resource", e);
            return "";
        }
    }
}
```

- [ ] **Step 2: 更新 ChatController 添加 Agent 接口**

```java
package com.aiflow.chat.controller;

import com.aiflow.chat.dto.ChatRequest;
import com.aiflow.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatRequest request) {
        return chatService.streamChat(request);
    }

    @PostMapping(value = "/agent", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> agentChat(@Valid @RequestBody ChatRequest request) {
        return chatService.streamAgentChat(request);
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/aiflow/chat/
git commit -m "feat: integrate RAG and Agent into chat module"
```

---

### Task 18: 最终验证

- [ ] **Step 1: 启动 Docker Compose**

```bash
docker compose up -d
```

- [ ] **Step 2: 运行数据库迁移**

```bash
mvn flyway:migrate
```

- [ ] **Step 3: 启动应用**

```bash
mvn spring-boot:run
```

- [ ] **Step 4: 测试登录接口**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

Expected: Return JWT token

- [ ] **Step 5: 测试聊天接口**

```bash
curl -X POST http://localhost:8080/chat/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"sessionId":1,"message":"Hello"}' \
  --no-buffer
```

Expected: SSE stream response

- [ ] **Step 6: 运行所有测试**

```bash
mvn test
```

Expected: All tests PASS

- [ ] **Step 7: 最终提交**

```bash
git add .
git commit -m "feat: complete AIFlow platform implementation"
```
