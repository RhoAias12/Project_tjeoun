package com.tjoeun.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
  private String userId;
  private String input;
}