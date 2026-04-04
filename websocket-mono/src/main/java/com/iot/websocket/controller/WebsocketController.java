package com.iot.websocket.controller;

import com.iot.websocket.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * WebSocket控制器
 *
 * 处理来自WebSocket客户端的STOMP消息。
 * 该类负责接收客户端发送的消息，并通过消息服务进行发布/订阅模式的处理。
 *
 * @Controller 表示这是一个Spring MVC控制器，用于处理消息
 */
@Controller
public class WebsocketController {

    private final MessageService messageService;

    public WebsocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 处理问候消息
     *
     * 接收来自客户端的问候消息，并通过消息服务发布到指定频道。
     * 该方法映射到"/app/greet"目的地（由WebSocketConfig中的setApplicationDestinationPrefixes("/app")配置）。
     *
     * @MessageMapping("/greet") 将方法映射到STOMP消息目的地，客户端发送消息到/app/greet时触发此方法
     * @param message 从客户端接收的消息内容
     * @Payload 注解标识方法参数为STOMP消息的有效负载
     *
     * WebSocket连接流程：
     * 1. 客户端连接到WebSocket端点(/stomp)
     * 2. 客户端发送STOMP消息到/app/greet
     * 3. 此方法接收消息并通过消息服务发布到GREETING_CHANNEL_OUTBOUND频道
     * 4. 消息服务监听器接收到消息后，通过WebSocket广播到/topic/greetings
     */
    @MessageMapping("/greet")
    public void greetMessage(@Payload String message) {
        messageService.publish("GREETING_CHANNEL_OUTBOUND", message);
    }
}