package com.iot.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WebSocket服务器应用程序启动类 - 可伸缩WebSocket服务器
 *
 * 这是可伸缩WebSocket微服务架构中的WebSocket服务器启动类。
 * 该服务负责：
 * - 处理客户端的WebSocket连接和STOMP消息
 * - 消费来自Backend服务的Redis Stream消息
 * - 向Backend服务发布事件消息
 * - 实现WebSocket与Redis Stream的桥接
 *
 * @SpringBootApplication 表示这是一个Spring Boot应用程序
 * @EnableScheduling 启用定时任务调度功能
 *
 * 在可伸缩架构中的角色：
 * Client <--WebSocket--> WebSocket Server <--Redis Stream--> Backend Service
 */
@EnableScheduling
@SpringBootApplication
public class WebsocketServerApplication {

    /**
     * 应用程序入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WebsocketServerApplication.class, args);
    }
}