package com.tjoeun.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeContentDTO {
  private String question;
  private String context;
}