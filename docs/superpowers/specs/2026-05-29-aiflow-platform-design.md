# AIFlow 智能知识库 Agent 平台 - 架构设计文档

## 1. 项目定位

AIFlow 是一个基于 Spring AI + RAG + pgvector 的 AI Native 智能知识库 Agent 平台。

**项目核心目标：**
- 实现企业知识库 RAG 检索增强生成
- 支持流式 AI 对话
- 支持文档向量化
- 支持 Tool Calling Agent
- 支持 Redis 会话记忆
- 体现 AI 工程化能力

**项目重点：**
- AI Engineering
- RAG Pipeline
- Vector Search
- Prompt Engineering
- Streaming Chat
- Tool Calling

**项目不追求：**
- 多 Agent 编排
- MCP
- LangGraph Workflow
- AI 模型训练
- 复杂前端

## 2. 技术栈

**后端：**
- Java 21
- Spring Boot 3.4.x
- Spring AI 1.0.x
- Spring WebFlux
- MyBatis-Plus
- PostgreSQL
- pgvector
- Redis
- RabbitMQ
- Flyway
- Docker Compose

**AI 能力：**
- OpenAI Compatible API
- DeepSeek Chat
- BGE-M3 Embedding
- RAG
- Tool Calling
- SSE Streaming

**前端（简单即可）：**
- Vue3
- Element Plus
- Markdown 渲染

## 3. 架构设计原则

**当前阶段：** 单体模块化架构

**原因：**
- 更适合 AI IDE 持续生成代码
- 降低模块依赖复杂度
- 避免 Maven 多模块上下文混乱
- 提升开发速度
- 聚焦 AI Pipeline

**后期：** 可演进为微服务

## 4. 项目目录结构

```plaintext
aiflow-platform
├── pom.xml
├── docker-compose.yml
├── src/main/java/com/aiflow
│
├── common
│   ├── config
│   ├── exception
│   ├── result
│   ├── util
│   └── constant
│
├── auth
│   ├── controller
│   ├── service
│   ├── filter
│   └── dto
│
├── chat
│   ├── controller
│   ├── service
│   ├── memory
│   ├── prompt
│   ├── dto
│   └── entity
│
├── rag
│   ├── service
│   ├── embedding
│   ├── retrieval
│   ├── parser
│   ├── chunk
│   ├── strategy
│   └── entity
│
├── agent
│   ├── service
│   ├── tool
│   └── prompt
│
├── file
│   ├── controller
│   ├── service
│   ├── consumer
│   └── entity
│
├── prompt
│   ├── system
│   ├── rag
│   └── tool
│
├── mapper
├── resources
│   ├── application.yml
│   ├── application-dev.yml
│   ├── mapper
│   ├── prompts
│   └── db/migration
│
└── AiflowApplication.java
```

## 5. 核心架构

**AI 对话架构：**
```plaintext
用户问题
-> Redis Memory 加载上下文
-> RAG 检索增强
-> Prompt Augmentation
-> Tool Calling
-> LLM Stream Generate
-> SSE 返回
-> Redis 保存会话
```

## 6. AI 对话模块设计

**Chat 核心职责：**
- SSE 流式输出
- 上下文记忆
- Prompt 拼接
- Agent 调用
- Token Usage 统计

**SSE 流式接口：**
```java
@PostMapping(value = "/chat/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chat(@RequestBody ChatRequest request)
```

**Chat 流程：**
```plaintext
用户提问
-> 加载最近会话
-> Token Window 裁剪
-> RAG 检索
-> Prompt 拼接
-> Tool Calling
-> ChatClient.stream()
-> SSE 返回
-> 保存聊天记录
-> 保存 Token Usage
```

## 7. Memory 设计

**Redis 会话记忆：**
```plaintext
key: chat:memory:{sessionId}
```

**Conversation Window：**
- 保留最近 10 轮对话
- 超过 token 上限自动裁剪

**后期可扩展：**
- summary memory
- semantic memory

## 8. Prompt 管理设计

Prompt 不允许硬编码，必须使用 `resources/prompts/` 目录。

**Prompt 目录：**
```plaintext
resources/prompts
├── system
│   └── system-prompt.txt
├── rag
│   └── rag-prompt.txt
└── tool
    └── tool-agent-prompt.txt
```

**Prompt 加载方式：**
```java
Resource resource = resourceLoader.getResource("classpath:prompts/rag/rag-prompt.txt");
```

**System Prompt：**
```plaintext
你是企业知识库 AI 助手。

请严格基于提供的上下文回答问题。

如果上下文不存在答案，请明确说明不知道。

禁止编造内容。
```

## 9. RAG 架构设计

**RAG Pipeline：**
```plaintext
上传文档
-> 文档解析
-> chunk 切片
-> overlap
-> embedding
-> pgvector 存储
```

**Query Pipeline：**
```plaintext
用户问题
-> query embedding
-> similarity search
-> retrieve topK
-> prompt augmentation
-> LLM generate
```

## 10. Chunk 策略

**当前策略：**
- chunk size = 500
- chunk overlap = 100

**目标：**
- 保持语义完整
- 避免上下文丢失

**后期扩展：**
- semantic chunk
- recursive split

## 11. Retrieval Strategy

**当前实现：**
- cosine similarity
- topK = 5

**后期扩展：**
- similarity threshold
- rerank
- hybrid search
- BM25 + vector

## 12. Embedding 设计

**Embedding 模型：** BGE-M3

**原因：**
- 开源
- 中文效果优秀
- 社区成熟
- 支持本地部署

## 13. PostgreSQL + pgvector 设计

**knowledge_chunk 表：**
```sql
CREATE TABLE knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT,
    chunk_index INT,
    content TEXT,
    embedding vector(1024),
    created_at TIMESTAMP
);
```

**pgvector 索引：**
```sql
CREATE INDEX idx_chunk_embedding
ON knowledge_chunk
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
```

**相似度查询：**
```sql
SELECT id, content, 1 - (embedding <=> #{vector}) AS similarity
FROM knowledge_chunk
ORDER BY embedding <=> #{vector}
LIMIT 5;
```

## 14. 文件上传模块设计

**上传流程：**
```plaintext
upload file
-> save metadata
-> send MQ message
-> async parse
-> chunk
-> embedding
-> save vector
```

**支持文件类型：** pdf, md, txt, docx

**文档解析器：**
```plaintext
DocumentParser
├── PdfParser
├── MarkdownParser
├── TxtParser
└── DocxParser
```

## 15. RabbitMQ 设计

**使用原因：** embedding 属于高耗时、高 IO、可异步操作，因此文件上传与向量化解耦。

**MQ 流程：**
```plaintext
文件上传 -> MQ -> 异步 embedding -> 批量写入 pgvector
```

**Exchange：** `rag.document.exchange`
**Queue：** `rag.document.parse.queue`

## 16. Tool Calling Agent 设计

**当前阶段：** 只实现简单 Tool Agent

**不实现：** 多 Agent、Auto Planning、Workflow Engine

**Tool 列表：**
- `WeatherTool` — `@Tool getWeather(String city)`
- `CourseSearchTool` — `@Tool queryCourse(String keyword)`

**Tool Calling 流程：**
```plaintext
用户问题
-> ChatClient + Tools
-> LLM 判断是否调用 Tool
-> Tool 返回结果
-> LLM 继续生成
```

## 17. Token Usage 统计

**chat_usage 表：**
```sql
CREATE TABLE chat_usage (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT,
    model_name VARCHAR(100),
    prompt_tokens INT,
    completion_tokens INT,
    total_tokens INT,
    created_at TIMESTAMP
);
```

**使用目的：**
- Token 成本统计
- AI 调用分析
- 后期限流设计

## 18. Docker Compose 设计

**端口映射：**

| 服务 | 端口 |
|------|------|
| PostgreSQL | 15432 |
| Redis | 16379 |
| RabbitMQ AMQP | 15672 |
| RabbitMQ Dashboard | 25672 |

## 19. 安全设计

**当前阶段：** JWT + Spring Security，不实现复杂 RBAC

**白名单：**
- `/auth/login`
- `/auth/register`
- `/doc.html`
- `/actuator/health`

## 20. 开发优先级

**第一阶段：**
- Spring AI 接入
- SSE Chat
- Redis Memory

**第二阶段：**
- 文件上传
- PDF 解析
- chunk
- embedding

**第三阶段：**
- pgvector
- similarity search
- RAG

**第四阶段：**
- Tool Calling
- MQ 异步 embedding
- Token Usage

## 21. AI IDE 开发规范

**代码生成要求：**
- 优先可运行
- 优先简单清晰
- 不过度抽象
- 使用 Spring 官方推荐 API
- 禁止复杂设计模式
- 禁止无意义封装

## 22. 编码规范

- Controller 不写业务逻辑
- Service 负责核心逻辑
- 使用构造器注入
- 禁止 Field Injection
- DTO / VO 分离
- 统一 Result 返回
- 统一异常处理

## 23. 项目最终目标

本项目用于：
- Java 后端实习简历
- AI 工程化能力展示
- RAG 能力展示
- Agent Tool Calling 展示
- 向量检索能力展示

**最终效果：** 项目既能真正运行，又能体现 AI Native 后端工程能力。
