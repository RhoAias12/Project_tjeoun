package com.tjoeun.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

  // OpenAI Chat Completion API URL
  private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

  // application.properties 또는 환경변수에서 안전하게 주입!
  @Value("${openai.api.key}")
  private String openaiApiKey;

  public String generateReply(String userInput) {
    RestTemplate restTemplate = new RestTemplate();

    // HTTP 헤더 구성
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth("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"); // 안전하게 주입한 값 사용
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    // 요청 payload
    Map<String, Object> payload = Map.of(
      "model", "gpt-4o",   // 원하는 모델 (예: gpt-3.5-turbo, gpt-4o)
      "messages", new Object[]{
        Map.of("role", "system", "content", "You are a helpful assistant."),
        Map.of("role", "user", "content", userInput)
      }
    );

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(
        OPENAI_API_URL, request, String.class);

      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response.getBody());

      // OpenAI의 응답에서 assistant 메시지 추출
      String reply = root.get("choices")
        .get(0)
        .get("message")
        .get("content")
        .asText();

      return reply.trim();

    } catch (Exception e) {
      e.printStackTrace();
      return "❌ 오류 발생: " + e.getMessage();
    }
  }
}