# 消息服务架构设计文档

## 概述

本文档描述了 `basic-websocket-server`（单体架构版本）项目中的消息服务架构设计。消息服务作为 WebSocket 与消息中间件之间的桥梁，负责处理消息的发布/订阅模式，实现实时消息的传递和广播。

### 设计目标

1. **解耦性**：业务逻辑与具体消息技术实现分离
2. **可扩展性**：支持多种消息中间件（内存、Redis、Kafka等）
3. **可维护性**：通过接口抽象，便于替换和测试
4. **高性能**：保证消息传递的实时性和可靠性

## 架构设计

### 核心接口

项目定义了顶层接口 `MessageService`，位于 `com.iot.websocket.service` 包中：

```java
public interface MessageService {
    void publish(String topic, String message);
    void subscribe(String channelTopic, String destination);
}
```

#### 接口职责

1. **`publish(String topic, String message)`**
   - 发布消息到指定频道
   - 所有订阅了该频道的订阅者都会收到消息
   - 同步操作，消息立即发送

2. **`subscribe(String channelTopic, String destination)`**
   - 订阅指定频道并转发到目标地址
   - 建立消息从源频道到目标地址的桥接关系
   - 支持一个频道有多个订阅者

### 当前实现：内存消息服务

#### 类结构

`MemoryMessageService` 实现了 `MessageService` 接口，使用内存数据结构提供消息服务：

```java
@Service
public class MemoryMessageService implements MessageService {
    private final ConcurrentHashMap<String, List<Consumer<String>>> channelSubscribers;
    // 实现细节...
}
```

#### 存储结构

1. **`ConcurrentHashMap<String, List<Consumer<String>>> channelSubscribers`**
   - 键：频道名称（String）
   - 值：订阅者列表（`CopyOnWriteArrayList<Consumer<String>>`）
   - 线程安全，支持并发访问

2. **订阅者模型**
   - 每个订阅者是一个 `Consumer<String>` 函数
   - 收到消息时执行转发逻辑到 WebSocket
   - 使用 `CopyOnWriteArrayList` 保证遍历时的线程安全

#### 消息流转

```
客户端发送消息
    ↓
WebSocketController.greetMessage()
    ↓
MessageService.publish("GREETING_CHANNEL_OUTBOUND", message)
    ↓
内存存储查找频道订阅者
    ↓
遍历订阅者列表，调用 Consumer.accept(message)
    ↓
WebSocketTemplate.convertAndSend("/topic/greetings", message)
    ↓
客户端接收消息
```

### 配置与初始化

#### Spring 配置

1. **WebSocket 配置**：`WebSocketConfig` 配置 STOMP 端点和消息代理
2. **服务注入**：`MemoryMessageService` 通过 `@Service` 注解注册为 Spring Bean
3. **依赖注入**：`WebsocketController` 通过构造函数注入 `MessageService`

#### 初始化流程

1. Spring 容器启动
2. 创建 `MemoryMessageService` 实例，注入 `SimpMessagingTemplate`
3. 执行 `@PostConstruct subscribe()` 方法，建立初始订阅关系：
   ```java
   subscribe("GREETING_CHANNEL_INBOUND", "/topic/greetings");
   ```

## 扩展设计

### Redis 实现方案

未来可以通过创建 `RedisMessageService` 实现 `MessageService` 接口，轻松切换到 Redis：

#### 实现类

```java
@Service
@ConditionalOnProperty(name = "message.service.type", havingValue = "redis")
public class RedisMessageService implements MessageService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    
    @Override
    public void publish(String topic, String message) {
        reactiveRedisTemplate.convertAndSend(topic, message).subscribe();
    }
    
    @Override
    public void subscribe(String channelTopic, String destination) {
        reactiveRedisTemplate.listenTo(ChannelTopic.of(channelTopic))
            .map(ReactiveSubscription.Message::getMessage)
            .subscribe(message -> websocketTemplate.convertAndSend(destination, message));
    }
}
```

#### 配置切换

```yaml
# application.yml
message:
  service:
    type: redis  # 可选：memory, redis, kafka
```

### 其他消息中间件

相同的接口可以支持其他消息中间件：

1. **Apache Kafka**：`KafkaMessageService`
2. **RabbitMQ**：`RabbitMQMessageService`
3. **Apache Pulsar**：`PulsarMessageService`

## 使用示例

### WebSocket 控制器

```java
@Controller
public class WebsocketController {
    private final MessageService messageService;
    
    @MessageMapping("/greet")
    public void greetMessage(@Payload String message) {
        messageService.publish("GREETING_CHANNEL_OUTBOUND", message);
    }
}
```

### REST API 通知

```java
@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private final SimpMessagingTemplate template;
    
    @PostMapping
    public void newMessage(@RequestBody NewMessageRequest request) {
        template.convertAndSend(request.getTopic(), request.getMessage());
    }
}
```

## 性能考虑

### 内存实现

1. **优点**：
   - 零网络开销，延迟极低
   - 无需外部依赖，部署简单
   - 适合单节点应用

2. **限制**：
   - 不支持分布式部署
   - 服务重启后消息丢失
   - 内存容量限制

### Redis 实现

1. **优点**：
   - 支持分布式部署
   - 消息持久化
   - 高可用性和扩展性

2. **开销**：
   - 网络延迟
   - Redis 服务器维护
   - 序列化/反序列化成本

## 测试策略

### 单元测试

1. **接口契约测试**：验证所有实现符合接口规范
2. **内存服务测试**：测试 `MemoryMessageService` 的消息传递
3. **控制器集成测试**：测试 WebSocket 端点消息流转

### 集成测试

1. **Redis 集成测试**：验证 Redis 实现的消息传递
2. **多节点测试**：验证分布式场景下的消息一致性

## 部署建议

### 开发环境

- 使用 `MemoryMessageService`，无需外部依赖
- 简化开发环境配置
- 快速启动和调试

### 生产环境

- 根据规模选择实现：单节点用内存，多节点用 Redis
- 配置监控和告警
- 考虑消息持久化需求

## 维护与监控

### 关键指标

1. **消息吞吐量**：每秒处理的消息数量
2. **延迟分布**：消息从发布到接收的时间
3. **错误率**：消息发送失败的比例
4. **内存使用**：频道和订阅者的内存占用

### 日志记录

- 消息发布和订阅的日志记录
- 错误和异常的详细日志
- 性能指标的定期记录

## 版本历史

| 版本 | 日期 | 描述 |
|------|------|------|
| 1.0 | 2026-04-01 | 初始版本，内存消息服务实现 |
| 1.1 | 2026-04-01 | 更新包名为 com.iot，升级 Spring Boot 到 3.2.12，Java 17 |

## 相关文档

1. [WebSocket 配置文档](basic-websocket-server/src/main/java/com/iot/websocket/config/WebSocketConfig.java)
2. [MessageService 接口定义](basic-websocket-server/src/main/java/com/iot/websocket/service/MessageService.java)
3. [MemoryMessageService 实现](basic-websocket-server/src/main/java/com/iot/websocket/service/MemoryMessageService.java)
4. [WebsocketController 使用示例](basic-websocket-server/src/main/java/com/iot/websocket/controller/WebsocketController.java)
