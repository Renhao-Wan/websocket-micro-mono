package com.iot.websocket.model;

/**
 * 新消息请求模型类 - 可伸缩版本
 *
 * 用于REST API接收WebSocket消息请求的数据结构。
 * 与基础版本相同，但在可伸缩架构中用于广播事件。
 */
public class NewMessageRequest {
    private String topic;
    private String message;

    public NewMessageRequest() {
    }

    public NewMessageRequest(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}