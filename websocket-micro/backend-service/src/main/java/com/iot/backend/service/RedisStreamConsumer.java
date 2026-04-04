package com.iot.backend.service;

import com.iot.common.model.StreamDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

/**
 * Redis Stream消费者服务
 *
 * 实现Redis Stream消息监听器，处理来自WebSocket服务器的事件消息。
 * 作为后端服务的消息入口点，负责接收和处理业务事件。
 *
 * @Service 表示这是一个Spring服务组件
 * StreamListener<String, ObjectRecord<String, StreamDataEvent>> 泛型参数说明：
 * - String: Redis Stream的键类型
 * - ObjectRecord<String, StreamDataEvent>: 消息记录类型，包含StreamDataEvent对象
 */
@Service
public class RedisStreamConsumer implements StreamListener<String, ObjectRecord<String, StreamDataEvent>> {

    private static final Logger logger = LoggerFactory.getLogger(RedisStreamConsumer.class);

    /**
     * 处理接收到的Stream消息
     *
     * 当Redis Stream中有新消息时，此方法被自动调用。
     * 记录接收到的消息内容，后续可以添加业务处理逻辑。
     *
     * @param record Redis Stream消息记录，包含消息内容和元数据
     *
     * @Override 表示实现StreamListener接口的抽象方法
     *
     * 消息处理流程：
     * 1. WebSocket服务器发布消息到TEST_EVENT_TO_BACKEND流
     * 2. RedisStreamConfig中的监听器接收到消息
     * 3. 此方法被调用，处理消息内容
     * 4. 记录日志，可扩展业务逻辑处理
     */
    @Override
    public void onMessage(ObjectRecord<String, StreamDataEvent> record) {
        logger.info("[NEW] --> received message: {} from stream: {}", record.getValue(), record.getStream());
    }
}