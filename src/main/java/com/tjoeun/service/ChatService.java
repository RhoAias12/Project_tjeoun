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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjoeun.entity.ChatHistory;
import com.tjoeun.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

  private static final Logger log = LoggerFactory.getLogger(ChatService.class);  // 로그 객체

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

    // 메시지 리스트 초기화
    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of(
      "role", "system",
      "content", "You are a helpful assistant. 모든 답변은 반드시 한국어로 100자 이내로 핵심만 요약해서 제공해줘."
    ));

    // 과거 대화 내용 추가
    for (ChatHistory h : historyList) {
      messages.add(Map.of("role", h.getRole(), "content", h.getContent()));
    }

    // 사용자 입력 추가
    messages.add(Map.of("role", "user", "content", prompt));

    // RestTemplate 설정
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(openaiApiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    // API 요청 페이로드
    Map<String, Object> payload = Map.of(
      "model", "gpt-4.1-nano",  // gpt-4 모델 사용 (모델 이름 확인)
      "messages", messages
    );

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    try {
      // OpenAI API 호출
      ResponseEntity<String> response = restTemplate.postForEntity(
        OPENAI_API_URL, request, String.class);

      // 응답 처리
      if (response.getBody() == null) {
        log.error("API 응답이 null입니다.");
        return "❌ API 응답이 없습니다.";
      }

      // JSON 파싱
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response.getBody());

      // OpenAI 응답에서 메시지 추출
      String reply = root.path("choices")
        .path(0)  // choices 배열의 첫 번째 요소
        .path("message")
        .path("content")
        .asText();

      if (reply.isEmpty()) {
        log.error("빈 응답이 반환되었습니다.");
        return "❌ OpenAI의 응답이 비어 있습니다.";
      }

      // Builder 패턴으로 안전하게 저장
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

      // DB에 대화 내용 저장
      chatHistoryRepository.saveAll(List.of(userMsg, botMsg));

      // 응답 반환
      return reply.trim();

    } catch (Exception e) {
      // 예외 처리 및 로깅
      log.error("OpenAI API 호출 중 오류 발생", e);
      return "❌ 오류 발생: " + e.getMessage();
    }
  }
}
