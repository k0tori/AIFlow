# AIFlow 智能知识库 Agent 平台 - 项目骨架设计文档

## 1. 项目概述

AIFlow 是一个基于 Spring AI + RAG + pgvector 的智能知识库 Agent 平台，支持文档上传、向量化、知识库问答、SSE 流式输出和 Tool Calling Agent。

## 2. 技术决策

| 决策点 | 选择 | 版本 |
|--------|------|------|
| 构建工具 | Maven | - |
| Java | 21 | - |
| Spring Boot | 3.4.x | 3.4.5 |
| Spring AI | 1.0.x (GA) | 1.0.0 |
| Chat 模型 | deepseek-chat | - |
| Embedding 模型 | DeepSeek | 1024 维 |
| 基础设施 | 统一 Docker Compose | - |
| 认证模式 | 独立 filter | - |
| 代码工具 | Lombok + MapStruct + Knife4j | - |
| 配置管理 | 主模块集中配置 | - |
| DB 迁移 | Flyway | - |
| 日志 | 基础 Logback | - |
| 测试 | JUnit 5 + Mockito | - |

## 3. 项目目录结构

```
aiflow-platform/
├── pom.xml                          # 父 POM
├── docker-compose.yml               # 基础设施
├── .gitignore
├── aiflow-common/                   # 公共模块
│   ├── pom.xml
│   └── src/main/java/com/aiflow/common/
│       ├── config/                  # Redis、WebFlux、Jackson 配置
│       ├── exception/               # 全局异常处理
│       ├── result/                  # Result 统一返回
│       ├── util/                    # JWT、工具类
│       └── constant/                # 常量
├── aiflow-auth/                     # 认证模块
│   ├── pom.xml
│   └── src/main/java/com/aiflow/auth/
│       ├── filter/                  # JWT Filter
│       ├── config/                  # Security 配置
│       └── service/                 # 认证服务
├── aiflow-chat/                     # AI 对话模块
│   ├── pom.xml
│   └── src/main/java/com/aiflow/chat/
│       ├── controller/
│       ├── service/
│       └── dto/
├── aiflow-rag/                      # RAG 核心模块
│   ├── pom.xml
│   └── src/main/java/com/aiflow/rag/
│       ├── service/
│       └── config/
├── aiflow-file/                     # 文件上传模块
│   ├── pom.xml
│   └── src/main/java/com/aiflow/file/
│       ├── controller/
│       ├── service/
│       └── entity/
├── aiflow-agent/                    # Agent 模块
│   ├── pom.xml
│   └── src/main/java/com/aiflow/agent/
│       ├── service/
│       └── config/
├── aiflow-tools/                    # 工具模块
│   ├── pom.xml
│   └── src/main/java/com/aiflow/tools/
│       ├── weather/
│       └── course/
├── aiflow-search/                   # 检索模块
│   ├── pom.xml
│   └── src/main/java/com/aiflow/search/
│       ├── service/
│       └── mapper/
└── aiflow-web/                      # 启动模块（主模块）
    ├── pom.xml
    └── src/main/
        ├── java/com/aiflow/web/
        │   └── AiflowApplication.java
        └── resources/
            ├── application.yml
            ├── application-dev.yml
            └── db/migration/        # Flyway 脚本
```

## 4. 模块依赖关系

```
aiflow-web（启动模块）
├── aiflow-chat
│   ├── aiflow-rag
│   │   ├── aiflow-search
│   │   │   └── aiflow-common
│   │   └── aiflow-common
│   └── aiflow-agent
│       ├── aiflow-tools
│       │   └── aiflow-common
│       └── aiflow-common
├── aiflow-file
│   └── aiflow-common
├── aiflow-auth
│   └── aiflow-common
└── aiflow-common
```

**依赖原则：**
- `aiflow-common` 是最底层模块，被所有模块依赖
- `aiflow-web` 是最顶层启动模块，聚合所有业务模块
- 业务模块之间通过接口通信，避免循环依赖
- `aiflow-tools` 只被 `aiflow-agent` 依赖

## 5. 父 POM 设计

```xml
<!-- aiflow-platform/pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
    </parent>

    <groupId>com.aiflow</groupId>
    <artifactId>aiflow-platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>aiflow-common</module>
        <module>aiflow-auth</module>
        <module>aiflow-chat</module>
        <module>aiflow-rag</module>
        <module>aiflow-file</module>
        <module>aiflow-agent</module>
        <module>aiflow-tools</module>
        <module>aiflow-search</module>
        <module>aiflow-web</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <knife4j.version>4.5.0</knife4j.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring AI BOM -->
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- 内部模块 -->
            <dependency>
                <groupId>com.aiflow</groupId>
                <artifactId>aiflow-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <!-- 其他模块类似 -->

            <!-- 第三方 -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <!-- 其他第三方 -->
        </dependencies>
    </dependencyManagement>
</project>
```

## 6. 基础配置类（aiflow-common）

| 类名 | 职责 |
|------|------|
| `Result<T>` | 统一返回封装 `{code, message, data}` |
| `GlobalExceptionHandler` | 全局异常处理（`@RestControllerAdvice`） |
| `BusinessException` | 业务异常基类 |
| `RedisConfig` | Redis 序列化配置 |
| `WebFluxConfig` | CORS、SSE 配置 |
| `JacksonConfig` | JSON 序列化配置（日期格式、Long→String） |
| `JwtUtil` | JWT 生成/解析/验证 |
| `SecurityConstants` | 权限相关常量 |

**Result 封装示例：**
```java
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) { ... }
    public static <T> Result<T> error(int code, String message) { ... }
}
```

## 7. Docker Compose 设计

```yaml
# docker-compose.yml
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

**端口映射：**

| 服务 | 宿主机端口 | 容器端口 | 说明 |
|------|-----------|----------|------|
| PostgreSQL | 15432 | 5432 | 容器内必须用 5432 |
| Redis | 16379 | 6379 | 容器内必须用 6379 |
| RabbitMQ AMQP | 15672 | 5672 | 容器内必须用 5672 |
| RabbitMQ 管理 | 25672 | 15672 | 容器内必须用 15672 |

## 8. 认证模块设计（aiflow-auth）

| 类名 | 职责 |
|------|------|
| `JwtAuthenticationFilter` | 拦截请求、解析 Token、设置 SecurityContext |
| `SecurityConfig` | Spring Security 配置、Filter 链注册 |
| `AuthService` | 登录验证、Token 生成 |
| `AuthController` | 登录接口 `POST /auth/login` |

**认证流程：**
```
请求 → JwtAuthenticationFilter
  ├─ 无 Token /login 路径 → 放行
  ├─ Token 有效 → 设置 SecurityContext → 继续
  └─ Token 无效 → 返回 401
```

**白名单路径（无需认证）：**
- `/auth/login`
- `/auth/register`
- `/doc.html`、`/webjars/**`（Knife4j 文档）
- `/actuator/health`

## 9. AI 对话模块设计（aiflow-chat）

| 类名 | 职责 |
|------|------|
| `ChatController` | 对话接口 `POST /chat/send`（SSE 流式） |
| `ChatService` | 调用 Spring AI ChatClient、管理上下文 |
| `ChatMemoryService` | Redis 存储会话历史 |
| `ChatRequest` / `ChatResponse` | DTO |

**SSE 流式流程：**
```
POST /chat/send (sessionId + message)
    ↓
Redis 加载历史消息
    ↓
拼接 Prompt（系统提示 + 历史 + 用户问题）
    ↓
调用 ChatClient.stream()
    ↓
Flux<String> → SSE 逐 token 推送
    ↓
Redis 保存本轮对话
```

**接口定义：**
```java
@PostMapping(value = "/chat/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chat(@RequestBody ChatRequest request)
```

## 10. RAG 模块设计（aiflow-rag + aiflow-search）

**RAG 查询流程：**
```
用户问题
    ↓
EmbeddingClient.embed(question) → 向量
    ↓
SearchService.similaritySearch(vector, topK) → List<Chunk>
    ↓
拼接 Prompt（系统提示 + 检索上下文 + 用户问题）
    ↓
ChatClient.stream(prompt) → SSE 流式返回
```

**核心组件：**

| 模块 | 类名 | 职责 |
|------|------|------|
| aiflow-rag | `RagService` | RAG 编排：embedding → 检索 → 增强 → 生成 |
| aiflow-rag | `EmbeddingService` | 调用 EmbeddingClient 生成向量 |
| aiflow-search | `SearchService` | pgvector 相似度查询 |
| aiflow-search | `ChunkMapper` | MyBatis-Plus 操作 knowledge_chunk 表 |

**相似度查询 SQL：**
```sql
SELECT id, document_id, chunk_index, content,
       1 - (embedding <=> #{queryVector}) AS similarity
FROM knowledge_chunk
ORDER BY embedding <=> #{queryVector}
LIMIT #{topK}
```

## 11. 文件上传模块设计（aiflow-file）

**文件处理流程：**
```
上传文件 (MultipartFile)
    ↓
FileController 保存文件元数据 → knowledge_document 表 (status=PENDING)
    ↓
发送 MQ 消息 → rag.document.parse.queue
    ↓
FileConsumer 异步消费：
  ├─ 解析文档（PDF/MD/TXT/DOCX）
  ├─ 文本清洗
  ├─ Chunk 切片（500 字符，100 overlap）
  ├─ 调用 EmbeddingService 生成向量
  ├─ 批量写入 knowledge_chunk 表
  └─ 更新 document status=COMPLETED
```

**核心组件：**

| 类名 | 职责 |
|------|------|
| `FileController` | 文件上传接口 `POST /file/upload` |
| `FileService` | 保存元数据、发送 MQ 消息 |
| `FileConsumer` | MQ 消费者、异步处理文档 |
| `DocumentParser` | 文档解析接口（策略模式） |
| `PdfParser` / `MarkdownParser` 等 | 各格式解析实现 |
| `ChunkSplitter` | 文本切片（500/100 策略） |

## 12. Agent 模块设计（aiflow-agent + aiflow-tools）

**Tool Calling 流程：**
```
用户问题 + 工具定义
    ↓
ChatClient.call(prompt, tools)
    ↓
Spring AI 自动判断是否调用工具
    ├─ 需要工具 → 调用对应 Tool → 返回结果 → 继续生成
    └─ 不需要工具 → 直接生成回答
```

**核心组件：**

| 类名 | 职责 |
|------|------|
| `AgentService` | Agent 编排、调用 ChatClient with Tools |
| `WeatherTool` | `@Tool` 注解，获取天气信息 |
| `CourseSearchTool` | `@Tool` 注解，搜索课程信息 |

**Tool 定义示例：**
```java
@Component
public class WeatherTool {
    @Tool(description = "获取指定城市的天气信息")
    public String getWeather(String city) {
        // 调用天气 API 或返回模拟数据
    }
}

@Component
public class CourseSearchTool {
    @Tool(description = "根据关键词搜索课程")
    public String queryCourse(String keyword) {
        // 查询课程数据
    }
}
```

## 13. 应用配置

```yaml
# application-dev.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:15432/aiflow
    username: aiflow
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  data:
    redis:
      host: localhost
      port: 16379

  rabbitmq:
    host: localhost
    port: 15672
    username: aiflow
    password: ${MQ_PASSWORD}

  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
      embedding:
        options:
          model: deepseek-embedding

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true

knife4j:
  enable: true
  setting:
    language: zh_cn
```

## 14. 设计验证

- [x] 目录结构与项目说明一致
- [x] 模块依赖关系无循环
- [x] 端口映射避免默认端口冲突
- [x] 认证模块独立解耦
- [x] RAG 流程完整（embedding → 检索 → 增强 → 生成）
- [x] 文件处理异步化（MQ）
- [x] SSE 流式输出设计
- [x] Tool Calling 集成 Spring AI
