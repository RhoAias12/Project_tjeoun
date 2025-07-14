package com.tjoeun.controller;

import com.tjoeun.dto.ChatRequest;
import com.tjoeun.dto.ChatResponse;
import com.tjoeun.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @PostMapping("/chat")
  public ChatResponse handleChat(@RequestBody ChatRequest request, Principal principal) {
    String input = request.getInput();
    String userId = (principal != null) ? principal.getName() : "anonymous";
    String reply = chatService.generateReply(userId, input);
    return new ChatResponse(reply);
  }

}