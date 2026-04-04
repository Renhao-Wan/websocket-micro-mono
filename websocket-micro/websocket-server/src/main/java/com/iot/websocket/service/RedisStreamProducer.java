package com.iot.websocket.service;

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
 * Redis Stream生产者服务 - WebSocket服务器
 *
 * 负责向Redis Stream发布事件消息到Backend服务。
 * 与Backend服务的生产者形成双向通信。
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
     * 将StreamDataEvent发布到指定的Redis Stream，供Backend服务消费。
     *
     * @param streamTopic Redis Stream名称
     * @param data 要发布的StreamDataEvent对象
     *
     * 消息流向：
     * WebSocket Controller -> 此方法 -> Redis Stream -> Backend Service
     */
    public void publishEvent(String streamTopic, StreamDataEvent data) {
        var record = StreamRecords.newRecord().ofObject(data).withStreamKey(streamTopic);
        reactiveRedisTemplate.opsForStream().add(record).subscribe();
    }

    /**
     * 定时发布测试消息到Backend服务
     *
     * 每5秒自动向Backend服务发送测试消息，用于演示双向通信。
     *
     * @Scheduled 配置定时任务：启动10秒后开始，每5秒执行一次
     *
     * 消息流向：
     * 此方法 -> TEST_EVENT_TO_BACKEND流 -> Backend服务
     */
    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    public void publishTestMessageToBackend() {
        StreamDataEvent data = new StreamDataEvent();
        data.setMessage("New Message from " + applicationName + " -- ID = " + atomicInteger.incrementAndGet());
        logger.info("Publishing Message: {} to Stream: TEST_EVENT_TO_BACKEND", data);
        publishEvent("TEST_EVENT_TO_BACKEND", data);
    }
}