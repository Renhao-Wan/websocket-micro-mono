package com.iot.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * WebSocket服务器应用程序测试类 - 基础版本
 *
 * 基础WebSocket服务器的Spring Boot测试，验证应用程序上下文能够正常加载。
 *
 * @SpringBootTest 注解表示这是一个Spring Boot集成测试
 */
@SpringBootTest
class WebsocketServerApplicationTests {

    /**
     * 测试应用程序上下文加载
     *
     * 验证Spring应用程序上下文能够成功启动，所有Bean能够正常注入。
     */
    @Test
    void contextLoads() {
    }

}