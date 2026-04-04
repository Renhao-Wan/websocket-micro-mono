package com.iot.websocket.model;

/**
 * 新消息请求模型类
 *
 * 用于表示通过REST API发送WebSocket消息的请求数据结构。
 * 包含目标主题和消息内容两个字段。
 *
 * 使用场景：
 * - NotificationController.newMessage()方法的请求体
 * - 外部系统通过HTTP POST发送WebSocket消息时的数据格式
 */
public class NewMessageRequest {
    private String topic;
    private String message;

    /**
     * 默认构造函数
     *
     * 供JSON反序列化使用
     */
    public NewMessageRequest() {
    }

    /**
     * 带参数的构造函数
     *
     * @param topic WebSocket目标主题，消息将发送到此主题
     * @param message 要发送的消息内容
     */
    public NewMessageRequest(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    /**
     * 获取目标主题
     *
     * @return WebSocket目标主题名称
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 设置目标主题
     *
     * @param topic WebSocket目标主题名称，如"/topic/notifications"
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 获取消息内容
     *
     * @return 要发送的消息文本内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置消息内容
     *
     * @param message 要发送的消息文本内容
     */
    public void setMessage(String message) {
        this.message = message;
    }
}