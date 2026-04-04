package com.iot.websocket.controller;

import com.iot.websocket.model.BroadcastEvent;
import com.iot.websocket.model.NewMessageRequest;
import com.iot.websocket.service.RedisBroadcastService;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器 - 可伸缩版本
 *
 * 提供REST API接口用于发送WebSocket通知消息。
 * 与基础版本不同，此版本使用RedisBroadcastService通过广播频道发送消息。
 *
 * @RestController 表示这是一个REST控制器
 * @RequestMapping("/api/notification") 映射HTTP请求到/api/notification路径
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final RedisBroadcastService redisBroadcastService;

    public NotificationController(RedisBroadcastService redisBroadcastService) {
        this.redisBroadcastService = redisBroadcastService;
    }

    /**
     * 处理新的消息请求
     *
     * 接收HTTP POST请求，通过Redis广播服务发送消息。
     * 与基础版本不同，此版本将消息转换为BroadcastEvent并通过Redis广播。
     *
     * @PostMapping 映射HTTP POST请求
     * @param request 消息请求对象
     * @RequestBody 将HTTP请求体反序列化为NewMessageRequest
     *
     * 消息流转路径：
     * REST API -> BroadcastEvent -> Redis广播频道 -> WebSocket客户端
     */
    @PostMapping
    public void newMessage(@RequestBody NewMessageRequest request) {
        BroadcastEvent event = new BroadcastEvent(request.getTopic(), request.getMessage());
        redisBroadcastService.publish(event);
    }
}