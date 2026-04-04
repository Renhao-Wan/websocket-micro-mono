package com.iot.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 后端服务应用程序启动类 - 可伸缩WebSocket架构的后端服务
 *
 * 这是可伸缩WebSocket微服务架构中的后端服务启动类。
 * 该服务负责：
 * - 消费来自WebSocket服务器的Redis Stream消息
 * - 产生测试消息到WebSocket服务器
 * - 作为业务逻辑处理层
 *
 * @SpringBootApplication 表示这是一个Spring Boot应用程序
 * @EnableScheduling 启用定时任务调度功能，用于定期发送测试消息
 *
 * 在可伸缩架构中的角色：
 * Backend Service <--Redis Stream--> WebSocket Server <--WebSocket--> Client
 */
@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    /**
     * 应用程序入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}