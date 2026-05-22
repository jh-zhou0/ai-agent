package org.zjh.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;

/**
 * langchain4j 调用 AI 大模型
 */
public class LangChainAiInvoke {

    public static void main(String[] args) {
        ChatModel qwenModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("deepseek-v4-flash")
                .build();
        String res = qwenModel.chat("你是谁？");
        System.out.println(res);
    }
}
