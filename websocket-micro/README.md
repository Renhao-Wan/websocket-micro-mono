# Scaling WebSocket Server

## 项目简介

这是一个可扩展的 WebSocket 微服务架构实现，采用 Redis 消息总线实现服务间通信。适合生产环境、高并发和企业级应用场景。

## 技术栈

| 技术 | 版本 |
|-----|------|
| Java | 17 |
| Spring Boot | 3.2.12 |
| Spring WebSocket | 3.2.12 |
| Spring WebFlux | 3.2.12 |
| Spring Data Redis Reactive | 3.2.12 |
| Redis | 7.x |
| STOMP 协议 | 1.2+ |
| 构建工具 | Maven |

## 项目结构

```
scaling-websocket-server/                        # 父 Maven 项目
├── pom.xml                                  # 父项目配置
├── common-dto/                              # 共享数据传输对象模块
│   ├── pom.xml
│   └── src/main/java/com/iot/common/model/
│       └── StreamDataEvent.java               # 统一事件模型
├── websocket-server/                          # WebSocket 服务器模块
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/iot/websocket/
│       │   ├── WebsocketServerApplication.java   # Spring Boot 主类
│       │   ├── config/
│       │   │   ├── RedisConfig.java          # Redis 配置
│       │   │   ├── RedisStreamConfig.java     # Redis Stream 配置
│       │   │   └── WebSocketConfig.java       # WebSocket 配置
│       │   ├── controller/
│       │   │   ├── NotificationController.java   # REST 通知 API
│       │   │   └── WebsocketController.java    # WebSocket 消息处理
│       │   ├── model/
│       │   │   ├── BroadcastEvent.java       # 广播事件模型
│       │   │   └── NewMessageRequest.java    # 消息请求模型
│       │   └── service/
│       │       ├── RedisBroadcastService.java # Redis 广播服务
│       │       ├── RedisStreamConsumer.java  # Redis Stream 消费者
│       │       └── RedisStreamProducer.java  # Redis Stream 生产者
│       └── test/java/com/iot/websocket/
│           └── WebsocketServerApplicationTests.java
├── backend-service/                            # 后端服务模块
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/iot/backend/
│       │   ├── BackendApplication.java      # Spring Boot 主类
│       │   ├── config/
│       │   │   └── RedisStreamConfig.java     # Redis Stream 配置
│       │   └── service/
│       │       ├── RedisStreamConsumer.java  # Redis Stream 消费者
│       │       └── RedisStreamProducer.java  # Redis Stream 生产者
│       └── test/java/com/iot/backend/
│           └── BackendApplicationTests.java
└── README.md                                   # 本文档
```

## 核心模块说明

### 1. common-dto（共享 DTO 模块）

提供 WebSocket 服务器和后端服务之间共享的数据传输对象。

- **StreamDataEvent**：Redis Stream 中传输的统一事件模型

### 2. websocket-server（WebSocket 服务器模块）

负责 WebSocket 连接管理和消息转发：

- **连接管理**：处理客户端 WebSocket 连接
- **消息消费**：从 Redis Stream 消费后端服务消息
- **消息发布**：向 Redis Stream 发布客户端消息
- **广播服务**：通过 Redis Pub/Sub 向所有实例广播消息

### 3. backend-service（后端服务模块）

负责业务逻辑处理：

- **消息生产**：定期产生测试消息到 WebSocket 服务器
- **消息消费**：从 Redis Stream 消费 WebSocket 服务器消息
- **业务处理**：处理和响应业务逻辑

## 消息传递机制

### Redis Stream（服务间可靠通信）

用于 WebSocket 服务器和后端服务之间的可靠消息传递：

```
WebSocket Server ←→ Redis Stream ←→ Backend Service
    (负载均衡)       (消费者组)
```

**特点**：
- 消息持久化
- 消费者组实现负载均衡
- 精确一次消费语义

### Redis Pub/Sub（服务器内部广播）

用于多个 WebSocket 服务器实例之间的实时广播：

```
任何 Instance ←→ Redis Pub/Sub ←→ 所有 Instances
```

**特点**：
- 实时广播到所有实例
- 适合内部通知和同步

## 快速开始

### 前置要求

- **JDK 17+**
- **Maven 3.6+**
- **Redis 7.x**

### 启动 Redis

```bash
# 启动 Redis 容器
docker run --name redis -p 6379:6379 -d redis:7.2

# 创建消费者组
docker exec redis redis-cli XGROUP CREATE TEST_EVENT_TO_BACKEND CONSUMER_GROUP $ MKSTREAM
```

### 构建项目

```bash
# 进入项目目录
cd scaling-websocket-server

# 构建所有模块
mvn clean package
```

### 启动多个 WebSocket 服务器实例

```bash
# 实例 A
mvn spring-boot:run -pl websocket-server -Dspring-boot.run.arguments="--server.port=8080,--spring.application.name=websocket-server-A"

# 实例 B
mvn spring-boot:run -pl websocket-server -Dspring-boot.run.arguments="--server.port=8181,--spring.application.name=websocket-server-B"
```

### 启动多个后端服务实例

```bash
# 实例 A
mvn spring-boot:run -pl backend-service -Dspring-boot.run.arguments="--server.port=18080,--spring.application.name=backend-A"

# 实例 B
mvn spring-boot:run -pl backend-service -Dspring-boot.run.arguments="--server.port=28080,--spring.application.name=backend-B"
```

### 验证运行状态

```bash
# 查看 Redis Stream 内容
docker exec redis redis-cli xrange TEST_EVENT_TO_BACKEND - +
docker exec redis redis-cli xrange TEST_EVENT_TO_WEBSOCKET_SERVER - +

# 查看消费者组状态
docker exec redis redis-cli XINFO GROUPS TEST_EVENT_TO_BACKEND
```

## 使用示例

### 1. WebSocket 客户端连接

```javascript
// 建立 WebSocket 连接到任意一个实例
const socket = new SockJS('http://localhost:8080/stomp');
const stompClient = Stomp.over(socket);

// 连接
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
});

// 订阅主题
stompClient.subscribe('/topic/to-frontend', function (message) {
    console.log('Received: ' + message.body);
});

// 发送消息
stompClient.send("/app/test", {}, JSON.stringify({content: 'Hello World'}));
```

### 2. REST API 发送通知

```bash
# 向任意 WebSocket 服务器实例发送通知
curl -X POST http://localhost:8080/api/notification \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "/topic/greetings",
    "message": "Notification from API"
  }'
```

## 配置说明

### application.yml (websocket-server)

```yaml
server:
  port: 8080

spring:
  application:
    name: websocket-server
  redis:
    host: localhost
    port: 6379

# 监控端点配置
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

### application.yml (backend-service)

```yaml
server:
  port: 18080

spring:
  application:
    name: backend
  redis:
    host: localhost
    port: 6379

# 监控端点配置
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

## 消息流转

```
┌───────────────────────────────────────────────────────────┐
│                     Redis 消息总线                          │
│              （Stream + Pub/Sub 混合模式）                │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────┐     ┌────────────────────────────┐ │
│  │  WebSocket 服务器      │◄────│  后端服务              │ │
│  │  (多实例负载均衡)        │      │ (消费者组，负载均衡)     │ │
│  │                        │      │                       │ │
│  │  ┌───────────────────┐  │  ┌────────────────────┐   │ │
│  │  │  连接管理          │  │  │  业务逻辑处理      │   │ │
│  │  │  • Session 粘性    │  │  │  • 消息处理      │   │ │
│  │  │  • 故障转移        │  │  │  • 定时任务      │   │ │
│  │  └───────────────────┘  │  └────────────────────┘   │ │
│  └─────────────────────────┘  └────────────────────────────┘ │
│  ┌─────────────────────┐     ┌─────────────────────────┐ │
│  │  Redis Stream 消费者 │     │  Redis Stream 生产者     │ │
│  │  ┌────────────────┐  │  └─────────────────────────┘ │
│  │  │ 消费者组    │  │  ┌────────────────────┐     │ │
│  │  │ 负载均衡     │  │  │  定时任务      │     │ │
│  │  │ 消息确认    │  │  └────────────────────┘     │ │
│  │  └────────────────┘  │                           │ │
│  └─────────────────────┘  └─────────────────────────────┤
└───────────────────────────────────────────────────────────────────┘
         │                              │
         ▼                              ▼
┌───────────────┐              ┌─────────────────────┐
│  Web 客户端    │              │     外部系统        │
│  (WebSocket)   │              │     (REST API)        │
└───────────────┘              └─────────────────────┘
```

## 架构特点

### 优势

- ✅ **水平扩展**：支持多实例部署，增加节点应对高并发
- ✅ **消息持久化**：Redis Stream 保证消息不丢失
- ✅ **高可用性**：多节点冗余，单点故障自动转移
- ✅ **负载均衡**：Redis Stream 消费者组自动分配消息
- ✅ **容量可扩展**：Redis 集群支持 TB 级数据
- ✅ **服务解耦**：WebSocket 服务器和后端服务可独立部署

### 限制

- ⚠️ **网络延迟**：Redis 通信引入网络开销
- ⚠️ **部署复杂**：需要协调 Redis 和多个服务
- ⚠️ **资源消耗高**：多进程需要更多计算资源
- ⚠️ **运维复杂**：需要监控和管理多个服务

## 适用场景

**推荐使用场景**：

- 🏢 **生产环境部署**：需要稳定可靠的生产级服务
- 👥 **高并发场景**：预期并发连接 > 1000
- 🏢 **企业级应用**：需要高可用性和可扩展性
- 📈 **需要持久化**：不能接受消息丢失
- 🔀 **水平扩展**：需要支持多实例部署
- 🚀 **微服务生态**：需要与现有微服务集成

**典型应用场景**：

- 💹 金融交易平台（实时报价、交易通知）
- 🌐 物联网数据流处理（海量设备连接）
- 📝 大规模实时协作（文档协同、白板）
- 💬 社交平台实时功能（聊天、通知、动态）
- 🎮 在线游戏后端（实时状态同步）

## 性能优化建议

### Redis 优化

1. **连接池配置**：合理配置连接池大小
2. **序列化优化**：使用高效的 JSON 序列化器
3. **Pipeline 批处理**：批量发送消息减少网络往返
4. **持久化配置**：根据数据量调整 AOF/RDB 策略

### 应用优化

1. **连接复用**：使用连接池避免频繁创建连接
2. **异步处理**：利用 Reactor 响应式编程模型
3. **缓存策略**：合理设置消息过期时间
4. **监控告警**：设置性能指标和异常告警

## 相关文档

- [项目对比分析](../docs/PROJECT_COMPARISON.md) - 与单体架构版本的详细对比

## 许可证

本项目基于 Apache License 2.0 开源协议。
