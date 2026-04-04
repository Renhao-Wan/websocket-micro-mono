package com.iot.backend.config;

import com.iot.common.model.StreamDataEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

/**
 * Redis Stream配置类 - 后端服务
 *
 * 配置Redis Stream监听器容器，用于消费来自WebSocket服务器的事件消息。
 * 使用消费者组模式实现负载均衡和消息确认机制。
 *
 * @Configuration 表示这是一个配置类
 */
@Configuration
public class RedisStreamConfig {

    private final StreamListener<String, ObjectRecord<String, StreamDataEvent>> streamListener;
    private final String applicationName;

    public RedisStreamConfig(StreamListener<String, ObjectRecord<String, StreamDataEvent>> streamListener,
                           @Value("${spring.application.name}") String applicationName) {
        this.streamListener = streamListener;
        this.applicationName = applicationName;
    }

    /**
     * 创建Redis Stream订阅Bean
     *
     * 配置Redis Stream监听器，使用消费者组模式消费消息。
     * 监听TEST_EVENT_TO_BACKEND流，接收来自WebSocket服务器的消息。
     *
     * @param redisConnectionFactory Redis连接工厂
     * @return 配置好的Stream订阅
     *
     * Redis Stream消费者组说明：
     * - CONSUMER_GROUP: 消费者组名称，允许多个消费者实例负载均衡
     * - applicationName: 当前消费者实例名称，用于标识消息由哪个实例消费
     * - ReadOffset.lastConsumed(): 从最后消费的位置开始读取
     * - receiveAutoAck: 自动确认模式，消息被消费后自动标记为已处理
     *
     * @Bean 注解将该方法返回的对象注册为Spring容器的Bean
     */
    @Bean
    public Subscription subscription(RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .targetType(StreamDataEvent.class)
                        .build();

        StreamMessageListenerContainer listenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        Subscription subscription = listenerContainer.receiveAutoAck(
                Consumer.from("CONSUMER_GROUP", applicationName),
                StreamOffset.create("TEST_EVENT_TO_BACKEND", ReadOffset.lastConsumed()),
                streamListener
        );

        listenerContainer.start();
        return subscription;
    }
}