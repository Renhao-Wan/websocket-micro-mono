# Basic WebSocket Server

## 项目简介

这是一个基础 WebSocket 服务器实现，采用单体架构设计，使用内存消息存储。适合学习、原型开发和小规模部署场景。

## 技术栈

| 技术 | 版本 |
|-----|------|
| Java | 17 |
| Spring Boot | 3.2.12 |
| Spring WebSocket | 3.2.12 |
| Spring WebFlux | 3.2.12 |
| STOMP 协议 | 1.2+ |
| 构建工具 | Maven |

## 项目结构

```
basic-websocket-server/
├── pom.xml                                      # Maven 项目配置
├── src/
│   ├── main/
│   │   ├── java/com/iot/websocket/
│   │   │   ├── WebsocketServerApplication.java      # Spring Boot 主类
│   │   │   ├── config/
│   │   │   │   └── WebSocketConfig.java           # WebSocket 配置
│   │   │   ├── controller/
│   │   │   │   ├── NotificationController.java       # REST 通知 API
│   │   │   │   └── WebsocketController.java        # WebSocket 消息处理
│   │   │   ├── model/
│   │   │   │   └── NewMessageRequest.java         # 消息请求模型
│   │   │   └── service/
│   │   │       ├── MessageService.java            # 消息服务接口
│   │   │       └── MemoryMessageService.java       # 内存消息服务实现
│   │   └── resources/
│   │       └── application.yml                    # 应用配置文件
│   └── test/
│       └── java/com/iot/websocket/
│           └── WebsocketServerApplicationTests.java
└── README.md                                    # 本文档
```

## 核心功能

### 1. WebSocket 实时通信

- **WebSocket 端点**：`ws://localhost:8080/stomp`
- **STOMP 子协议**：支持消息路由和订阅
- **消息代理**：支持 `/topic/*` 和 `/app/*` 路径

### 2. 内存消息服务

使用 `MemoryMessageService` 实现发布/订阅模式：

- **发布消息**：将消息发送到指定频道，所有订阅者立即收到
- **订阅频道**：监听频道并将消息转发到 WebSocket 目的地
- **线程安全**：使用 `ConcurrentHashMap` 和 `CopyOnWriteArrayList` 保证并发安全

### 3. REST API 通知

提供 HTTP 接口发送 WebSocket 通知：

```bash
POST http://localhost:8080/api/notification
Content-Type: application/json

{
  "topic": "/topic/greetings",
  "message": "Hello from REST API"
}
```

## 快速开始

### 前置要求

- **JDK 17+**
- **Maven 3.6+**

### 构建项目

```bash
# 进入项目目录
cd basic-websocket-server

# 清理并编译
mvn clean package

# 运行项目
mvn spring-boot:run
```

### 访问应用

应用启动后，可通过以下方式访问：

- **WebSocket 端点**：`ws://localhost:8080/stomp`
- **管理端点**：`http://localhost:8080/actuator/health`
- **默认端口**：8080

## 使用示例

### 1. WebSocket 客户端连接

```javascript
// 建立 WebSocket 连接
const socket = new SockJS('http://localhost:8080/stomp');
const stompClient = Stomp.over(socket);

// 连接
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
});

// 订阅主题
stompClient.subscribe('/topic/greetings', function (message) {
    console.log('Received: ' + message.body);
});

// 发送消息
stompClient.send("/app/greet", {}, JSON.stringify({content: 'Hello World'}));
```

### 2. REST API 发送通知

```bash
curl -X POST http://localhost:8080/api/notification \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "/topic/greetings",
    "message": "Notification from API"
  }'
```

## 配置说明

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: websocket-server

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

### WebSocket 配置 (WebSocketConfig.java)

- **端点注册**：注册 `/stomp` WebSocket 端点
- **消息代理**：配置 `/topic/*` 作为消息代理前缀
- **应用前缀**：配置 `/app/*` 作为客户端发送前缀

## 架构特点

### 优势

- ✅ **架构简单**：单体应用，易于理解和维护
- ✅ **零网络延迟**：内存消息存储，无外部依赖
- ✅ **快速启动**：无需外部服务，启动迅速
- ✅ **开发友好**：调试方便，适合学习和原型

### 限制

- ⚠️ **单点故障**：单个实例故障导致服务中断
- ⚠️ **内存限制**：受限于 JVM 内存容量
- ⚠️ **无持久化**：服务重启后消息丢失
- ⚠️ **扩展受限**：无法水平扩展，只能垂直扩展

## 消息流转

```
┌─────────────────────────────────────────────────────────┐
│                    WebSocket Server                 │
│  ┌───────────────────────────────────────────┐    │
│  │         消息服务架构                 │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  MessageService (接口)           │  │    │
│  │  │  └─────────────────────────────┘    │  │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  MemoryMessageService          │    │  │    │
│  │  │  • ConcurrentHashMap           │    │  │    │
│  │  │  • CopyOnWriteArrayList        │    │  │    │
│  │  └──────────────────────────────────────┘  │    │    │
│  └───────────────────────────────────────────┘    │    │
├───────────────────────────────────────────────────┤    │
│  控制器层                                   │    │
│  ┌───────────────────────────────────────────┐    │    │
│  │  • WebsocketController              │    │    │
│  │  • NotificationController           │    │    │
│  └───────────────────────────────────────────┘    │    │
│  ┌───────────────────────────────────────────┐    │    │
│  │  WebSocket 模板                   │    │    │
│  │  • SimpMessagingTemplate            │    │    │
│  └───────────────────────────────────────────┘    │
│  ┌───────────────────────────────────────────┐    │    │
│  │  内存消息存储                   │    │    │
│  │  • 频道映射                      │    │    │
│  │  • 订阅者列表                   │    │    │
│  └───────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
         │                  │                   ▼
    ┌───────────────┐   ┌─────────────────┐
    │  Web 客户端    │   │  外部系统      │
    │  (WebSocket)   │   │  (REST API)    │
    └───────────────┘   └─────────────────┘
```

## 适用场景

**推荐使用场景**：

- 🎓 **学习和教学**：理解 WebSocket 基本原理
- 🧪 **原型开发**：快速验证概念和功能
- 👥 **小团队项目**：资源有限，追求快速上线
- 📊 **低并发应用**：并发连接 < 1000
- 🧪 **内部工具**：监控仪表板、内部通知系统

**不推荐场景**：

- ❌ 生产环境高并发场景
- ❌ 需要消息持久化的系统
- ❌ 需要水平扩展的应用

## 相关文档

- [消息服务架构设计](../docs/MESSAGE_SERVICE_ARCHITECTURE.md) - 消息服务详细设计
- [项目对比分析](../docs/PROJECT_COMPARISON.md) - 与微服务架构版本的对比

## 许可证

本项目基于 Apache License 2.0 开源协议。
