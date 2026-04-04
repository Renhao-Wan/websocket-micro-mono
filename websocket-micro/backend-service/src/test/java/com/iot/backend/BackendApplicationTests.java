package com.iot.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 后端服务应用程序测试类
 *
 * 可伸缩WebSocket架构中Backend服务的Spring Boot测试，验证应用程序上下文能够正常加载。
 *
 * @SpringBootTest 注解表示这是一个Spring Boot集成测试
 */
@SpringBootTest
class BackendApplicationTests {

    /**
     * 测试应用程序上下文加载
     *
     * 验证Spring应用程序上下文能够成功启动，包括Redis Stream配置和定时任务。
     */
    @Test
    void contextLoads() {
    }

}