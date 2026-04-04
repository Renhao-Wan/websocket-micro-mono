package com.iot.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类 - WebSocket服务器
 *
 * 配置响应式Redis模板，使用JSON序列化方式。
 * 支持Spring Boot 3.x的Spring Data Redis API。
 *
 * @Configuration 表示这是一个配置类
 */
@Configuration
public class RedisConfig {

    /**
     * 创建响应式Redis模板Bean
     *
     * 配置使用JSON序列化器的ReactiveRedisTemplate，用于Redis Stream操作。
     * 支持将对象序列化为JSON格式并存储到Redis中。
     *
     * @param reactiveRedisConnectionFactory Redis连接工厂
     * @return 配置好的ReactiveRedisTemplate
     *
     * 序列化配置：
     * - Key序列化器: StringRedisSerializer，直接作为字符串存储
     * - Value序列化器: GenericJackson2JsonRedisSerializer，使用JSON格式
     * - Hash Key序列化器: StringRedisSerializer
     * - Hash Value序列化器: GenericJackson2JsonRedisSerializer
     *
     * 这种配置方式兼容Spring Data Redis 3.x的新API
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
            ObjectMapper objectMapper) {
        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .value(new GenericJackson2JsonRedisSerializer(objectMapper))
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(new GenericJackson2JsonRedisSerializer(objectMapper))
                .build();

        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
    }
}
