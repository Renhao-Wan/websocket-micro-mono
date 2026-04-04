package com.iot.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket消息代理配置类 - 可伸缩版本
 *
 * 配置WebSocket消息代理和STOMP端点，与基础版本相同但支持可伸缩架构。
 * 此配置允许WebSocket服务器处理客户端连接并路由消息。
 *
 * @Configuration 表示这是一个配置类
 * @EnableWebSocketMessageBroker 启用WebSocket消息代理功能
 * WebSocketMessageBrokerConfigurer 接口提供配置WebSocket消息代理的方法
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 注册STOMP端点
     *
     * 配置WebSocket连接的端点，客户端通过ws://host:port/stomp建立连接。
     * 在可伸缩架构中，此端点与基础版本保持一致。
     *
     * @param registry STOMP端点注册表
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp").setAllowedOrigins("*");
    }

    /**
     * 配置消息代理
     *
     * 设置消息路由规则，与基础版本相同但在可伸缩架构中承担桥接角色：
     * - /topic: 消息代理前缀，用于广播消息到客户端
     * - /app: 应用程序前缀，用于处理客户端发送的消息
     *
     * @param registry 消息代理注册表
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}