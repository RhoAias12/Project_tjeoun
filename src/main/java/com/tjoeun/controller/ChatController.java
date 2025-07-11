package com.tjoeun.controller;

import com.tjoeun.dto.ChatRequest;
import com.tjoeun.dto.ChatResponse;
import com.tjoeun.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @PostMapping("/chat")
  public ChatResponse handleChat(@RequestBody ChatRequest request) {
    String reply = chatService.generateReply(request.getInput());
    return new ChatResponse(reply);
  }
}