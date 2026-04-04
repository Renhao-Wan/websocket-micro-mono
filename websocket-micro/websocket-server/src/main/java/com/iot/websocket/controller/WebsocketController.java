package com.iot.websocket.controller;

import com.iot.common.model.StreamDataEvent;
import com.iot.websocket.service.RedisStreamProducer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * WebSocket控制器 - 可伸缩版本
 *
 * 处理来自WebSocket客户端的STOMP消息，与基础版本不同，
 * 此版本将消息通过Redis Stream发送到Backend服务进行处理。
 *
 * @Controller 表示这是一个Spring MVC控制器
 */
@Controller
public class WebsocketController {

    private final RedisStreamProducer redisStreamProducer;

    public WebsocketController(RedisStreamProducer redisStreamProducer) {
        this.redisStreamProducer = redisStreamProducer;
    }

    /**
     * 处理测试消息
     *
     * 接收来自客户端的消息，通过Redis Stream转发到Backend服务。
     * 与基础版本不同，此版本使用StreamDataEvent和Redis Stream进行服务间通信。
     *
     * @MessageMapping("/test") 映射到/app/test目的地
     * @param message 从客户端接收的消息内容
     * @Payload 标识方法参数为STOMP消息的有效负载
     *
     * 消息流转路径：
     * 客户端 -> /app/test -> StreamDataEvent -> Redis Stream -> Backend服务
     */
    @MessageMapping("/test")
    public void greetMessage(@Payload String message) {
        StreamDataEvent event = new StreamDataEvent();
        event.setMessage(message);
        redisStreamProducer.publishEvent("TEST_EVENT_TO_BACKEND", event);
    }
}