package com.tjoeun.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeContentDTO {
  private Integer resumeContentIdx;
  private String question;
  private String context;
}