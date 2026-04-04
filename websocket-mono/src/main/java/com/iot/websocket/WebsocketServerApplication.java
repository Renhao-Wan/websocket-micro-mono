package com.iot.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WebSocket服务器应用程序启动类 - 基础WebSocket服务器
 *
 * 这是基础WebSocket微服务的主启动类，负责启动Spring Boot应用程序。
 * 该服务提供基本的WebSocket功能，包括：
 * - STOMP协议支持的WebSocket消息传递
 * - Redis发布/订阅集成
 * - REST API接口用于发送通知
 *
 * @SpringBootApplication 注解表示这是一个Spring Boot应用程序，
 * 包含@Configuration、@EnableAutoConfiguration和@ComponentScan功能
 */
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