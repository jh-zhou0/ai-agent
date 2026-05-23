package org.zjh.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kayson
 * @date 2026/5/23
 */
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String uuid = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是Kayson";
        String answer = loveApp.doChat(message, uuid);
        Assertions.assertNotNull(answer);
        // 第一轮
        message = "如何让我的另一半（Fang）更爱我";
        loveApp.doChat(message, uuid);
        Assertions.assertNotNull(answer);
        // 第一轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        loveApp.doChat(message, uuid);
        Assertions.assertNotNull(answer);
    }
}