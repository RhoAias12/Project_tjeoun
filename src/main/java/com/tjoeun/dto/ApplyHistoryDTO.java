package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyHistoryDTO {

  private Integer applyHistoryId;

  private String statusDisplay;

  private String recruitmentTitle;

  private String recruitmentCompany;

  private Long recruitmentId;

  private Integer resumeId;

  private String userNickname;

  private String userEmail;
}
