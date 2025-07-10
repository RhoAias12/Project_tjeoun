package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyHistoryResumeContentDTO {
  private Long id;       // ApplyHistoryResumeContent PK
  private String question;
  private String context;
}
