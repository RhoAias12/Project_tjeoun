package com.tjoeun.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjoeun.entity.ChatHistory;
import com.tjoeun.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatHistoryRepository chatHistoryRepository;

  @Value("${openai.api.key}")
  private String openaiApiKey;

  private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

  public String generateReply(String userId, String userInput) {
    // 사용자 입력
    String prompt = userInput;

    // 과거 대화 불러오기
    List<ChatHistory> historyList = chatHistoryRepository
      .findTop10ByUserIdOrderByCreatedAtAsc(userId);

    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of(
      "role", "system",
      "content", "You are a helpful assistant. 모든 답변은 반드시 한국어로 100자 이내로 핵심만 요약해서 제공해줘."
    ));

    for (ChatHistory h : historyList) {
      messages.add(Map.of("role", h.getRole(), "content", h.getContent()));
    }

    messages.add(Map.of("role", "user", "content", prompt));

    // RestTemplate
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(openaiApiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    Map<String, Object> payload = Map.of(
      "model", "gpt-4o",
      "messages", messages
    );

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(
        OPENAI_API_URL, request, String.class);

      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response.getBody());

      String reply = root.get("choices")
        .get(0)
        .get("message")
        .get("content")
        .asText();

      // Builder 패턴으로 안전하게 저장!
      ChatHistory userMsg = ChatHistory.builder()
        .userId(userId)
        .role("user")
        .content(prompt)
        .createdAt(LocalDateTime.now())
        .build();

      ChatHistory botMsg = ChatHistory.builder()
        .userId(userId)
        .role("assistant")
        .content(reply)
        .createdAt(LocalDateTime.now())
        .build();

      chatHistoryRepository.saveAll(List.of(userMsg, botMsg));

      return reply.trim();

    } catch (Exception e) {
      e.printStackTrace();
      return "❌ 오류 발생: " + e.getMessage();
    }
  }
}