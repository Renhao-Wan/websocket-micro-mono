package com.iot.websocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * 广播事件模型类
 *
 * 用于Redis发布/订阅模式的事件数据结构，用于在WebSocket服务器内部广播消息。
 * 实现Serializable接口以支持序列化。
 *
 * 使用场景：
 * - Redis广播频道中的消息格式
 * - 从REST API接收的消息转换为广播事件
 * - 通过Redis广播到WebSocket客户端
 *
 * @JsonProperty 注解用于JSON序列化和反序列化
 * Serializable 接口支持对象序列化
 */
public class BroadcastEvent implements Serializable {

    /**
     * WebSocket目标主题
     * 指定广播消息应该发送到哪个WebSocket主题
     */
    @JsonProperty("topic")
    private String topic;

    /**
     * 广播消息内容
     * 要发送的具体消息文本
     */
    @JsonProperty("message")
    private String message;

    public BroadcastEvent() {
    }

    public BroadcastEvent(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    /**
     * 获取目标主题
     *
     * @return WebSocket目标主题
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 设置目标主题
     *
     * @param topic WebSocket目标主题
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 获取消息内容
     *
     * @return 消息文本内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置消息内容
     *
     * @param message 消息文本内容
     */
    public void setMessage(String message) {
        this.message = message;
    }
}