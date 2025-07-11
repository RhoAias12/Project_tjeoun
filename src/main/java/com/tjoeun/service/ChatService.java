package com.tjoeun.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

  public String generateReply(String userInput) {
    // 예: 진짜 AI 연동 or 임시 답변
    return "AI 답변: [" + userInput + "] 잘 받았습니다!";
  }
}