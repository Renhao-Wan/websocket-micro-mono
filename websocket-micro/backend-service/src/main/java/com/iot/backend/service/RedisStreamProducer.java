package com.iot.backend.service;

import com.iot.common.model.StreamDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis Stream生产者服务
 *
 * 负责向Redis Stream发布事件消息，主要向WebSocket服务器发送消息。
 * 包含定时任务，定期产生测试消息用于演示和测试。
 *
 * @Service 表示这是一个Spring服务组件
 */
@Service
public class RedisStreamProducer {

    private static final AtomicInteger atomicInteger = new AtomicInteger(0);
    private static final Logger logger = LoggerFactory.getLogger(RedisStreamProducer.class);

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final String applicationName;

    public RedisStreamProducer(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                              @Value("${spring.application.name}") String applicationName) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.applicationName = applicationName;
    }

    /**
     * 发布事件到Redis Stream
     *
     * 将StreamDataEvent对象发布到指定的Redis Stream。
     * 使用响应式编程模型，异步发送消息到流中。
     *
     * @param streamTopic Redis Stream名称
     * @param data 要发布的StreamDataEvent对象
     *
     * 工作流程：
     * 1. 创建Stream记录，包装StreamDataEvent对象
     * 2. 使用ReactiveRedisTemplate将记录添加到指定流
     * 3. 订阅操作以异步执行
     *
     * 消息流向：
     * Backend Service -> Redis Stream -> WebSocket Server -> Client
     */
    public void publishEvent(String streamTopic, StreamDataEvent data) {
        var record = StreamRecords.newRecord().ofObject(data).withStreamKey(streamTopic);
        reactiveRedisTemplate.opsForStream().add(record).subscribe();
    }

    /**
     * 定时发布测试消息到WebSocket服务器
     *
     * 每5秒自动执行一次，向WebSocket服务器发送测试消息。
     * 用于演示和测试Redis Stream的消息传递功能。
     *
     * @Scheduled 注解配置定时任务：
     * - initialDelay = 10000: 应用启动10秒后开始执行
     * - fixedRate = 5000: 每5秒执行一次
     *
     * 消息内容包含：
     * - 目标主题：/topic/to-frontend
     * - 消息内容：包含应用名称和自增ID
     *
     * 完整的消息流向：
     * 1. 此方法定时产生消息 -> TEST_EVENT_TO_WEBSOCKET_SERVER流
     * 2. WebSocket服务器消费消息 -> 转发到/topic/to-frontend
     * 3. 客户端订阅/topic/to-frontend接收消息
     */
    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    public void publishTestMessageToBackend() {
        StreamDataEvent data = new StreamDataEvent(
                "/topic/to-frontend",
                "New Message from " + applicationName + " -- ID = " + atomicInteger.incrementAndGet()
        );
        logger.info("Publishing Message: {} to Stream: TEST_EVENT_TO_WEBSOCKET_SERVER", data);
        publishEvent("TEST_EVENT_TO_WEBSOCKET_SERVER", data);
    }
}