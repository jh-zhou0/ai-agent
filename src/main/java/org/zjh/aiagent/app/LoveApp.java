package org.zjh.aiagent.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import org.zjh.aiagent.advisor.MyLoggerAdvisor;
import org.zjh.aiagent.chatmemory.FileBasedChatMemoryRepository;

import java.util.List;

/**
 * @author kayson
 * @date 2026/5/23
 */
@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    private static final String JSON_PROMPT = "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表";

    public LoveApp(ChatModel dashScopeChatModel) {
        // 初始化基于内存的对话记忆
        ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
        // 初始化基于文件的对话记忆
        String dir = System.getProperty("user.dir") + "/chat-memory";
        FileBasedChatMemoryRepository fileBasedChatMemoryRepository = new FileBasedChatMemoryRepository(dir);
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(chatMemoryRepository)
                .chatMemoryRepository(fileBasedChatMemoryRepository)
                .maxMessages(10)
                .build();
        Advisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        MyLoggerAdvisor myLoggerAdvisor = new MyLoggerAdvisor();
        this.chatClient = ChatClient
                .builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(messageChatMemoryAdvisor, myLoggerAdvisor)
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = this.chatClient
                .prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        if (chatResponse == null) {
            return "服务异常";
        }
        return chatResponse.getResult().getOutput().getText();
    }

    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI恋爱报告功能（支持结构化输出）
     *
     * @param message message
     * @param chatId chatId
     * @return LoveReport
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        return this.chatClient
                .prompt()
                .user(message)
                .system(SYSTEM_PROMPT + JSON_PROMPT)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
    }
}
