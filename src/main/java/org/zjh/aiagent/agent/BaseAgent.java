package org.zjh.aiagent.agent;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现step方法
 *
 * @author kayson
 * @since 2026/5/27 14:49
 */
@Slf4j
@Data
public abstract class BaseAgent {

    // 名称（核心属性）
    private String name;

    // 提示
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int currentStep = 1;
    private int maxStep = 10;

    // 循环次数
    private int duplicateThreshold = 2;

    // LLM
    private ChatClient chatClient;

    // Memory（自主维护上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 执行（运行代理）
     *
     * @param userPrompt 用户输入
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (state != AgentState.IDLE) {
            throw new RuntimeException("Can not run agent in state: " + state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Can not run agent with empty prompt");
        }
        // 更改状态
        state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 执行步骤，保存结果列表
        List<String> resultList = new ArrayList<>();
        try {
            while (currentStep < maxStep && state != AgentState.FINISHED) {
                log.info("Running step: {} / {}", currentStep, maxStep);
                // 单步执行
                String stepResult = step();
                String result = "Step " + currentStep + ": " + stepResult;
                log.info("Step {} result: {}", currentStep, result);
                currentStep++;
                // 检查是否陷入循环
                if (isStuck()) {
                    handleStuckState();
                }
                resultList.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxStep) {
                state = AgentState.IDLE;
                currentStep = 1;
                String terminatedResult = "Terminated: max step" + maxStep + " reached, stopping agent";
                resultList.add(terminatedResult);
            }
            return StrUtil.join("\n", resultList);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error running agent", e);
            return "Error running agent: " + e.getMessage();
        } finally {
            cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return SseEmitter实例
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建SseEmitter，设置较长的超时时间
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误：无法从状态运行代理: " + this.state);
                    emitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("错误：不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }

                // 更改状态
                state = AgentState.RUNNING;
                // 记录消息上下文
                messageList.add(new UserMessage(userPrompt));

                try {
                    for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step {}/{}", stepNumber, maxStep);

                        // 单步执行
                        String stepResult = step();
                        String result = "Step " + stepNumber + ": " + stepResult;

                        // 发送每一步的结果
                        emitter.send(result);
                    }
                    // 检查是否超出步骤限制
                    if (currentStep >= maxStep) {
                        state = AgentState.FINISHED;
                        emitter.send("执行结束: 达到最大步骤 (" + maxStep + ")");
                    }
                    // 正常完成
                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("执行智能体失败", e);
                    try {
                        emitter.send("执行错误: " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    // 清理资源
                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }


    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){}

    /**
     * 处理陷入循环的状态
     */
    protected void handleStuckState() {
        String stuckPrompt = "观察到重复响应。考虑新策略，避免重复已尝试过的无效路径。";
        this.nextStepPrompt = stuckPrompt + "\n" + (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        log.info("Agent detected stuck state. Added prompt: {}", stuckPrompt);
    }

    /**
     * 检查代理是否陷入循环
     *
     * @return 是否陷入循环
     */
    protected boolean isStuck() {
        if (this.messageList.size() < 2) {
            return false;
        }

        Message lastMessage = this.messageList.getLast();
        if (StrUtil.isBlank(lastMessage.getText())) {
            return false;
        }

        // 计算重复内容出现次数
        int duplicateCount = 0;
        for (int i = this.messageList.size() - 2; i >= 0; i--) {
            Message msg = this.messageList.get(i);
            if (msg.getMessageType() == MessageType.ASSISTANT &&
                    lastMessage.getText().equals(msg.getText())) {
                duplicateCount++;
            }
        }

        return duplicateCount >= this.duplicateThreshold;
    }
}
