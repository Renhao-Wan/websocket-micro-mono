package com.iot.websocket.service;

import com.iot.common.model.StreamDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis Stream消费者服务 - WebSocket服务器
 *
 * 实现Redis Stream消息监听器，处理来自Backend服务的事件消息。
 * 将Stream消息转换为WebSocket消息并发送给客户端。
 *
 * @Service 表示这是一个Spring服务组件
 * StreamListener<String, ObjectRecord<String, StreamDataEvent>> 泛型参数：
 * - String: Redis Stream的键类型
 * - ObjectRecord<String, StreamDataEvent>: 包含StreamDataEvent的消息记录
 */
@Service
public class RedisStreamConsumer implements StreamListener<String, ObjectRecord<String, StreamDataEvent>> {

    private static final Logger logger = LoggerFactory.getLogger(RedisStreamConsumer.class);

    private final SimpMessagingTemplate websocketTemplate;

    public RedisStreamConsumer(SimpMessagingTemplate websocketTemplate) {
        this.websocketTemplate = websocketTemplate;
    }

    /**
     * 处理接收到的Stream消息
     *
     * 当Redis Stream中有新消息时，此方法被自动调用。
     * 将StreamDataEvent中的消息转发到对应的WebSocket主题。
     *
     * @param record Redis Stream消息记录，包含StreamDataEvent和元数据
     *
     * @Override 表示实现StreamListener接口的抽象方法
     *
     * 消息桥接流程：
     * 1. Backend服务发布消息到TEST_EVENT_TO_WEBSOCKET_SERVER流
     * 2. RedisStreamConfig中的监听器接收到消息
     * 3. 此方法被调用，提取StreamDataEvent
     * 4. 检查目标主题是否有效
     * 5. 通过WebSocket模板转发到指定主题
     * 6. 客户端接收到消息
     *
     * 完整的消息流转路径：
     * Backend Service -> Redis Stream -> 此服务 -> WebSocket -> Client
     */
    @Override
    public void onMessage(ObjectRecord<String, StreamDataEvent> record) {
        logger.info("[NEW] --> received message: {} from stream: {}", record.getValue(), record.getStream());
        if (record.getValue().getTopic() != null) {
            websocketTemplate.convertAndSend(record.getValue().getTopic(), record.getValue().getMessage());
        }
    }
}