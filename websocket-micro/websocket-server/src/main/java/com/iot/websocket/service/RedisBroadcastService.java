package com.iot.websocket.service;

import com.iot.websocket.model.BroadcastEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Redis广播服务
 *
 * 实现Redis发布/订阅模式与WebSocket的集成，专门处理广播事件。
 * 与基础版本的RedisService类似，但专门处理BroadcastEvent类型。
 *
 * @Service 表示这是一个Spring服务组件
 */
@Service
public class RedisBroadcastService {

    private static final String BROADCAST_CHANNEL = "BROADCAST-CHANNEL";
    private static final Logger logger = LoggerFactory.getLogger(RedisBroadcastService.class);

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final SimpMessagingTemplate websocketTemplate;

    public RedisBroadcastService(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                SimpMessagingTemplate websocketTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.websocketTemplate = websocketTemplate;
    }

    /**
     * 发布广播事件到Redis频道
     *
     * 将BroadcastEvent发布到Redis广播频道，实现发布/订阅模式。
     * 所有订阅了该频道的服务都会收到此事件。
     *
     * @param event 要发布的广播事件
     *
     * 消息流转路径：
     * NotificationController -> 此方法 -> Redis广播频道 -> subscribe()方法 -> WebSocket客户端
     */
    public void publish(BroadcastEvent event) {
        logger.info("Broadcasting event... {}", event);
        reactiveRedisTemplate.convertAndSend(BROADCAST_CHANNEL, event).subscribe();
    }

    /**
     * 初始化Redis频道订阅
     *
     * 在Bean初始化完成后自动订阅Redis广播频道，
     * 当收到广播事件时转发到对应的WebSocket主题。
     *
     * @PostConstruct 注解表示该方法在依赖注入完成后执行
     *
     * 消息桥接流程：
     * Redis广播频道 -> BroadcastEvent -> WebSocket主题 -> 客户端
     */
    @PostConstruct
    public void subscribe() {
        reactiveRedisTemplate.listenTo(ChannelTopic.of(BROADCAST_CHANNEL))
                .map(ReactiveSubscription.Message::getMessage)
                .filter(BroadcastEvent.class::isInstance)
                .map(BroadcastEvent.class::cast)
                .subscribe(event -> websocketTemplate.convertAndSend(event.getTopic(), event.getMessage()));
    }
}