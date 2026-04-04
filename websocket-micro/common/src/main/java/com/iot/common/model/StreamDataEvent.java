package com.iot.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 流式数据事件模型类
 *
 * 用于在Redis Stream中传输的数据结构，表示一个事件消息。
 * 这是WebSocket微服务架构中的核心数据传输对象，在backend-service和websocket-server之间共享。
 *
 * 使用场景：
 * - Redis Stream中的消息格式
 * - Backend服务产生的事件
 * - WebSocket服务器消费并转发的事件
 *
 * @JsonProperty 注解用于JSON序列化和反序列化
 */
public class StreamDataEvent {

    /**
     * 消息内容
     * 事件的具体消息文本
     */
    @JsonProperty("message")
    private String message;

    /**
     * 目标主题
     * 指定消息应该发送到哪个WebSocket主题
     */
    @JsonProperty("topic")
    private String topic;

    public StreamDataEvent() {
    }

    public StreamDataEvent(String topic, String message) {
        this.topic = topic;
        this.message = message;
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
     * @param topic WebSocket目标主题，如"/topic/notifications"
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 重写toString方法，用于调试和日志记录
     *
     * @return 对象的字符串表示形式
     */
    @Override
    public String toString() {
        return "StreamDataEvent{" +
                "message='" + message + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }
}