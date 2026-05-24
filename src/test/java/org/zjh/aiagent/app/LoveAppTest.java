package org.zjh.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @author kayson
 * @date 2026/5/23
 */
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
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

    @Test
    void doChatWithReport() {
        String uuid = UUID.randomUUID().toString();
        String message = "你好，我是Kayson，如何让我的另一半（Fang）更爱我";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, uuid);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithReportAndResourceFile() {
        String uuid = UUID.randomUUID().toString();
        String message = "你好，我该如何让我的另一半（Ting）更爱我";
        String user = "空山";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReportAndResourceFile(message, uuid, user);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String uuid = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer = loveApp.doChatWithRag(message, uuid);
        Assertions.assertNotNull(answer);
    }
}