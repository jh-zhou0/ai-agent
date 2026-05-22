package org.zjh.aiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Http 调用 AI 大模型
 */
public class HttpAiInvoke {

    public static void main(String[] args) {
        String apiKey = TestApiKey.API_KEY;
        String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

        // 构建 message
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "你是谁");

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-v4-flash");
        requestBody.put("messages", new Object[]{message});
        requestBody.put("stream", true);
        requestBody.put("enable_thinking", true);

        // 发送 POST 请求
        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(requestBody))
                .timeout(30000)  // 设置超时30秒
                .execute();

        // 处理响应
        if (response.isOk()) {
            System.out.println(response.body());
        } else {
            System.err.println("请求失败: " + response.getStatus() + " - " + response.body());
        }

        response.close();
    }
}
