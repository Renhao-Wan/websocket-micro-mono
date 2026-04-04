package com.iot.websocket.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 内存消息服务类
 *
 * 实现基于内存的发布/订阅模式与WebSocket的集成。
 * 作为消息中间层，连接WebSocket和内存存储，实现异步消息传递。
 * 实现MessageService接口，提供与Redis相同的功能但无需外部依赖。
 * 使用内存数据结构，适合单节点部署或轻量级应用场景。
 *
 * @Service 表示这是一个Spring服务组件
 */
@Service
public class MemoryMessageService implements MessageService {

    private final SimpMessagingTemplate websocketTemplate;

    /**
     * 存储频道与订阅者列表的映射
     *
     * 使用ConcurrentHashMap保证线程安全的频道存储，每个频道对应一个CopyOnWriteArrayList，
     * 确保在遍历订阅者时修改操作的安全性和一致性。
     */
    private final ConcurrentHashMap<String, List<Consumer<String>>> channelSubscribers = new ConcurrentHashMap<>();

    /**
     * 构造函数，依赖注入SimpMessagingTemplate
     *
     * SimpMessagingTemplate是Spring WebSocket的核心组件，用于发送消息到WebSocket客户端。
     * 通过构造函数注入，实现依赖倒置原则。
     *
     * @param websocketTemplate Spring WebSocket消息模板，用于向客户端发送消息
     */
    public MemoryMessageService(SimpMessagingTemplate websocketTemplate) {
        this.websocketTemplate = websocketTemplate;
    }

    /**
     * 发布消息到指定频道
     *
     * 将消息发布到指定的内存频道，实现发布/订阅模式。
     * 消息会同步发送给该频道的所有订阅者。
     *
     * @param topic 频道名称
     * @param message 要发布的消息内容
     *
     * 工作流程：
     * 1. WebsocketController接收到客户端消息
     * 2. 调用此方法将消息发布到内存频道
     * 3. 内存存储将消息推送给所有订阅者
     * 4. 订阅者中的监听器接收到消息并转发到WebSocket
     */
    @Override
    public void publish(String topic, String message) {
        List<Consumer<String>> subscribers = channelSubscribers.get(topic);
        if (subscribers != null && !subscribers.isEmpty()) {
            // 遍历所有订阅者并发送消息
            subscribers.forEach(subscriber -> subscriber.accept(message));
        }
    }

    /**
     * 订阅频道并转发到WebSocket
     *
     * 监听指定的内存频道，当收到消息时自动转发到WebSocket目的地。
     * 实现内存消息到WebSocket的桥接功能。
     *
     * @param channelTopic 要监听的频道名称
     * @param destination WebSocket目的地路径，如"/topic/greetings"
     *
     * 消息流转说明：
     * 内存频道 -> 此方法监听器 -> WebSocket目的地 -> 客户端
     */
    @Override
    public void subscribe(String channelTopic, String destination) {
        // 创建订阅者：将消息转发到WebSocket目的地
        Consumer<String> subscriber = message -> websocketTemplate.convertAndSend(destination, message);

        // 将订阅者添加到频道的订阅者列表，如果列表不存在则创建
        channelSubscribers.computeIfAbsent(channelTopic, k -> new CopyOnWriteArrayList<>()).add(subscriber);
    }

    /**
     * 初始化订阅
     *
     * 在Bean初始化完成后自动执行，建立内存消息到WebSocket的消息桥接。
     * 订阅GREETING_CHANNEL_INBOUND频道，将消息转发到/topic/greetings目的地。
     *
     * @PostConstruct 注解表示该方法在依赖注入完成后执行
     *
     * 完整的消息流转路径：
     * 1. 客户端 -> /app/greet -> WebsocketController.greetMessage()
     * 2. WebsocketController -> MemoryMessageService.publish("GREETING_CHANNEL_OUTBOUND")
     * 3. 内存存储 -> GREETING_CHANNEL_INBOUND (假设其他服务发布到此频道)
     * 4. 此方法 -> websocketTemplate.convertAndSend("/topic/greetings")
     * 5. 客户端订阅/topic/greetings接收消息
     */
    @PostConstruct
    public void subscribe() {
        subscribe("GREETING_CHANNEL_INBOUND", "/topic/greetings");
    }
}