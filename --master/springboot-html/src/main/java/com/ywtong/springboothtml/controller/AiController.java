package com.ywtong.springboothtml.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final String SESSION_ROLE = "currentUserRole";

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.api-url}")
    private String apiUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    @PostMapping("/analyze")
    public Map<String, String> analyze(@RequestBody Map<String, Object> payload,
                                       HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        if (!"ROLE_ADMIN".equals(role)) {
            throw new RuntimeException("无权限使用AI分析功能");
        }

        String userMessage = (String) payload.get("message");
        String statsContext = (String) payload.getOrDefault("context", "");

        // 构建系统提示词
        String systemPrompt = "你是一个农产品电商平台的数据分析助手。" +
            "请根据提供的平台运营数据，给出简洁、实用的经营分析和建议。" +
            "回答请控制在200字以内，使用中文，语言简洁专业。";

        // 构建完整的用户消息（数据上下文 + 用户问题）
        String fullMessage = statsContext.isEmpty()
            ? userMessage
            : "当前平台数据：\n" + statsContext + "\n\n用户问题：" + userMessage;

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", fullMessage);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        // 发送请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            Map responseBody = response.getBody();

            // 解析响应
            List<Map> choices = (List<Map>) responseBody.get("choices");
            Map firstChoice = choices.get(0);
            Map message = (Map) firstChoice.get("message");
            String content = (String) message.get("content");

            Map<String, String> result = new HashMap<>();
            result.put("reply", content);
            return result;
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("reply", "AI分析服务暂时不可用，请稍后重试。错误：" + e.getMessage());
            return error;
        }
    }
}
