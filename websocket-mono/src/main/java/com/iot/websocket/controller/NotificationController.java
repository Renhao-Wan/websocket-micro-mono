package com.iot.websocket.controller;

import com.iot.websocket.model.NewMessageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 *
 * 提供REST API接口用于发送WebSocket通知消息。
 * 外部系统可以通过HTTP POST请求调用此接口，将消息发送到指定的WebSocket主题。
 *
 * @RestController 表示这是一个REST控制器，包含@Controller和@ResponseBody功能
 * @RequestMapping("/api/notification") 映射HTTP请求到/api/notification路径
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final SimpMessagingTemplate template;

    /**
     * 构造函数，依赖注入SimpMessagingTemplate
     *
     * SimpMessagingTemplate是Spring WebSocket的核心组件，用于发送消息到WebSocket客户端。
     * 通过构造函数注入，实现依赖倒置原则。
     *
     * @param template Spring WebSocket消息模板，用于向客户端发送消息
     */
    public NotificationController(SimpMessagingTemplate template) {
        this.template = template;
    }

    /**
     * 处理新的消息请求
     *
     * 接收HTTP POST请求，将消息发送到指定的WebSocket主题。
     * 这是WebSocket消息的入口点，外部系统通过此API发送实时通知。
     *
     * @PostMapping 映射HTTP POST请求
     * @param request 消息请求对象，包含目标主题和消息内容
     * @RequestBody 注解将HTTP请求体反序列化为NewMessageRequest对象
     *
     * 工作流程：
     * 1. 外部系统发送POST请求到/api/notification
     * 2. 该方法接收请求并提取主题和消息
     * 3. 使用SimpMessagingTemplate将消息发送到指定主题
     * 4. 所有订阅了该主题的WebSocket客户端都会收到消息
     */
    @PostMapping
    public void newMessage(@RequestBody NewMessageRequest request) {
        template.convertAndSend(request.getTopic(), request.getMessage());
    }
}