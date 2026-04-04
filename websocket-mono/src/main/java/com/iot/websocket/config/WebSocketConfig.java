package com.iot.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket消息代理配置类
 *
 * 配置WebSocket消息代理和STOMP端点，实现WebSocket与STOMP协议的集成。
 * STOMP(Simple Text Oriented Messaging Protocol)是一种简单的消息协议，
 * 用于在客户端和服务器之间进行异步消息传递。
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
     * 配置WebSocket连接的端点，客户端通过这个端点建立WebSocket连接。
     * "/stomp"是WebSocket连接的端点路径，客户端需要连接到ws://host:port/stomp
     * setAllowedOrigins("*")允许所有来源的跨域请求，在生产环境中应该配置具体的域名
     *
     * @param registry STOMP端点注册表，用于注册WebSocket端点
     *
     * @Override 表示重写接口方法
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp").setAllowedOrigins("*");
    }

    /**
     * 配置消息代理
     *
     * 设置消息代理的前缀和应用程序目标前缀，定义消息路由规则：
     * - enableSimpleBroker("/topic"): 启用简单的内存消息代理，"/topic"前缀的消息会被路由到消息代理
     * - setApplicationDestinationPrefixes("/app"): 设置应用程序目标前缀，"/app"前缀的消息会被路由到@MessageMapping注解的方法
     *
     * 消息流向说明：
     * 1. 客户端发送消息到/app/greet -> 路由到WebsocketController.greetMessage()
     * 2. 服务器发送消息到/topic/greetings -> 广播给所有订阅了该主题的客户端
     *
     * @param registry 消息代理注册表，用于配置消息代理设置
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}