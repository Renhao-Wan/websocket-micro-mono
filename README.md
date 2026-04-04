# WebSocket 实战项目

[![CI 矩阵测试](https://github.com/Renhao-Wan/websocket-micro-mono/actions/workflows/ci.yml/badge.svg)](https://github.com/Renhao-Wan/websocket-micro-mono/actions/workflows/ci.yml)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 项目简介

本项目展示了 WebSocket 实时通信的两种架构方案：单体架构（basic-websocket-server）和微服务架构（scaling-websocket-server）。适合不同规模和需求的应用场景。

- **单体架构版本**：适合学习、原型开发和小规模部署
- **微服务架构版本**：适合生产环境、高并发和企业级应用

## 技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Java | 17 | 开发语言（从 11 升级） |
| Spring Boot | 3.2.12 | 微服务框架（从 2.5.6 升级） |
| Spring WebSocket | 3.2.12 | WebSocket 通信支持 |
| STOMP 协议 | 1.2+ | WebSocket 子协议，提供消息路由 |
| Spring WebFlux | 3.2.12 | 响应式编程支持 |
| Spring Data Redis Reactive | 3.2.12 | Redis 响应式访问（扩展版） |
| Redis | 7.x | 消息中间件（扩展版） |
| Maven | 3.6+ | 项目构建工具 |

## 项目结构

```
websocket-microservice/
├── basic-websocket-server/           # 单体架构 WebSocket 服务器
│   ├── pom.xml
│   └── src/main/java/com/iot/websocket/
│       ├── WebsocketServerApplication.java
│       ├── config/WebSocketConfig.java
│       ├── controller/
│       ├── model/NewMessageRequest.java
│       └── service/
│           ├── MessageService.java          # 消息服务接口
│           └── MemoryMessageService.java     # 内存消息服务实现
│
├── scaling-websocket-server/           # 微服务架构 WebSocket 系统
│   ├── pom.xml                        # 父 Maven 项目
│   ├── common-dto/                    # 共享 DTO 模块
│   │   └── src/main/java/com/iot/common/model/
│   │       └── StreamDataEvent.java
│   ├── websocket-server/               # WebSocket 服务器模块
│   │   └── src/main/java/com/iot/websocket/
│   │       ├── config/
│   │       │   ├── RedisConfig.java
│   │       │   ├── RedisStreamConfig.java
│   │       │   └── WebSocketConfig.java
│   │       ├── controller/
│   │       ├── model/
│   │       │   ├── BroadcastEvent.java
│   │       │   └── NewMessageRequest.java
│   │       └── service/
│   │           ├── RedisBroadcastService.java
│   │           ├── RedisStreamConsumer.java
│   │           └── RedisStreamProducer.java
│   └── backend-service/               # 后端服务模块
│       └── src/main/java/com/iot/backend/
│           ├── config/RedisStreamConfig.java
│           └── service/
│               ├── RedisStreamConsumer.java
│               └── RedisStreamProducer.java
│
├── docs/                               # 项目文档
│   ├── MESSAGE_SERVICE_ARCHITECTURE.md   # 消息服务架构设计
│   └── PROJECT_COMPARISON.md             # 项目对比分析
│
└── README.md                           # 本文档
```

## 架构对比

| 方面 | basic-websocket-server (单体) | scaling-websocket-server (微服务) |
|-----|---------------------------|-----------------------------|
| **架构模式** | 单体应用 | 微服务架构 |
| **消息存储** | 内存（易失性） | Redis Stream（持久化） |
| **扩展方式** | 垂直扩展 | 水平扩展 |
| **部署复杂度** | 简单（单 JAR） | 复杂（多服务 + Redis） |
| **负载均衡** | 不支持 | 支持（消费者组） |
| **高可用性** | 单点故障 | 多节点冗余 |
| **适用规模** | 小规模（<1000 连接） | 大规模（>1000 连接） |
| **开发成本** | 低 | 高 |
| **运维成本** | 低 | 高 |
| **学习曲线** | 平缓 | 陡峭 |

## 快速开始

### 基本要求

- **JDK 17+**
- **Maven 3.6+**
- **Redis 7.x**（仅扩展版需要）

### 选择项目版本

根据您的需求选择合适的项目：

```
是否需要生产环境？
├── 否 → 选择 basic-websocket-server（单体）
└── 是 → 
    ↓
    预期并发连接数？
    ├── < 1000 → basic-websocket-server 可能足够
    └── > 1000 → 
        ↓
        选择 scaling-websocket-server（微服务）
```

### 运行单体版本

```bash
# 进入单体项目目录
cd basic-websocket-server

# 构建并运行
mvn clean spring-boot:run

# 访问
# WebSocket: ws://localhost:8080/stomp
# API: http://localhost:8080/actuator/health
```

### 运行微服务版本

```bash
# 1. 启动 Redis
docker run --name redis -p 6379:6379 -d redis:7.2

# 2. 创建消费者组
docker exec redis redis-cli XGROUP CREATE TEST_EVENT_TO_BACKEND CONSUMER_GROUP $ MKSTREAM

# 3. 构建项目
cd scaling-websocket-server
mvn clean package

# 4. 启动多个 WebSocket 服务器实例
mvn spring-boot:run -pl websocket-server -Dspring-boot.run.arguments="--server.port=8080,--spring.application.name=websocket-server-A" &
mvn spring-boot:run -pl websocket-server -Dspring-boot.run.arguments="--server.port=8181,--spring.application.name=websocket-server-B" &

# 5. 启动多个后端服务实例
mvn spring-boot:run -pl backend-service -Dspring-boot.run.arguments="--server.port=18080,--spring.application.name=backend-A" &
mvn spring-boot:run -pl backend-service -Dspring-boot.run.arguments="--server.port=28080,--spring.application.name=backend-B" &
```

## 文档说明

### [basic-websocket-server/README.md](basic-websocket-server/README.md)

单体架构版本的详细文档：

- **快速开始**：构建和运行指南
- **项目结构**：完整的目录结构说明
- **使用示例**：WebSocket 客户端和 REST API 示例
- **配置说明**：application.yml 和 WebSocket 配置
- **架构特点**：单体架构的优势和限制

### [scaling-websocket-server/README.md](scaling-websocket-server/README.md)

微服务架构版本的详细文档：

- **快速开始**：完整的部署和运行指南
- **项目结构**：多模块的详细结构说明
- **核心模块**：common-dto、websocket-server、backend-service 说明
- **消息机制**：Redis Stream + Pub/Sub 混合模式详解
- **配置说明**：各模块的配置文件说明
- **性能优化**：Redis 和应用层的优化建议

### [docs/MESSAGE_SERVICE_ARCHITECTURE.md](docs/MESSAGE_SERVICE_ARCHITECTURE.md)

消息服务架构设计文档：

- **接口设计**：MessageService 顶层接口定义
- **内存实现**：MemoryMessageService 详细实现说明
- **扩展设计**：Redis 和其他消息中间件的扩展方案
- **部署建议**：开发和生产环境的部署策略

### [docs/PROJECT_COMPARISON.md](docs/PROJECT_COMPARISON.md)

项目对比分析文档：

- **详细对比**：两个项目在各个维度的详细对比
- **技术差异**：技术栈和架构差异分析
- **决策指南**：如何根据需求选择合适的项目版本
- **升级路径**：从单体到微服务的迁移建议

## 使用场景建议

### 选择单体版本的情况

✅ **推荐使用单体版本（basic-websocket-server）**：
- 🎓 学习和教学环境
- 🧪 原型开发和概念验证
- 👥 小团队或个人项目
- 📊 低并发场景（<1000 连接）
- 🧪 内部工具或监控系统
- 不需要消息持久化

### 选择微服务版本的情况

✅ **推荐使用微服务版本（scaling-websocket-server）**：
- 🏢 生产环境部署
- 👥 高并发场景（>1000 连接）
- 🏢 企业级应用系统
- 📈 需要高可用性和可扩展性
- 🔒 需要消息持久化保证
- 🔀 需要水平扩展能力
- 🚀 微服务生态集成需求

## 贡献指南

本项目欢迎贡献！在提交 Pull Request 前，请确保：

1. 代码符合项目的代码风格
2. 添加适当的单元测试
3. 更新相关文档
4. 提交信息清晰描述变更内容

## 许可证

本项目基于 Apache License 2.0 开源协议。

## 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 提交 Issue
- 发起 Discussion
- 提交 Pull Request

---

**最后更新时间**：2026-04-01
**版本**：1.0.0
