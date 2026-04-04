package com.iot.websocket.config;

import com.iot.common.model.StreamDataEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

/**
 * Redis Stream配置类 - WebSocket服务器
 *
 * 配置Redis Stream监听器容器，用于消费来自Backend服务的事件消息。
 * 与Backend服务的配置不同，此配置使用非消费者组模式，直接消费最新消息。
 *
 * @Configuration 表示这是一个配置类
 */
@Configuration
public class RedisStreamConfig {

    private final StreamListener<String, ObjectRecord<String, StreamDataEvent>> streamListener;

    public RedisStreamConfig(StreamListener<String, ObjectRecord<String, StreamDataEvent>> streamListener) {
        this.streamListener = streamListener;
    }

    private StreamMessageListenerContainer<String, ObjectRecord<String, StreamDataEvent>> initListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .targetType(StreamDataEvent.class)
                        .build();
        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }

    /**
     * 创建Redis Stream订阅Bean
     *
     * 配置Redis Stream监听器，监听来自Backend服务的消息。
     * 使用StreamOffset.latest()从最新消息开始消费，不遗漏任何消息。
     *
     * @param redisConnectionFactory Redis连接工厂
     * @return 配置好的Stream订阅
     *
     * Redis Stream消费模式说明：
     * - 非消费者组模式：直接接收所有消息
     * - StreamOffset.latest(): 从流的最新位置开始消费
     * - 监听TEST_EVENT_TO_WEBSOCKET_SERVER流
     *
     * 消息流转路径：
     * Backend服务 -> TEST_EVENT_TO_WEBSOCKET_SERVER流 -> 此监听器 -> RedisStreamConsumer
     *
     * @Bean("TestEventSubscription") 指定Bean名称以便区分
     */
    @Bean("TestEventSubscription")
    public Subscription subscription(RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainer<String, ObjectRecord<String, StreamDataEvent>> listenerContainer =
                initListenerContainer(redisConnectionFactory);
        Subscription subscription = listenerContainer.receive(
                StreamOffset.latest("TEST_EVENT_TO_WEBSOCKET_SERVER"), streamListener);
        listenerContainer.start();
        return subscription;
    }
}