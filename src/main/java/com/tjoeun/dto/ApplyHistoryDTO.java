package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

  private Long applyHistoryResumeId;

  private String userNickname;

  private String userEmail;

  // 추가 필드들 (Recruitment)
  private LocalDate recruitmentDeadline;
  private String recruitmentLocation;
  private String recruitmentLogoUrl;

  private String status;

  // 추가 필드들 (Resume)
  private String resumeTitle;
  private String resumeImg;
  private String resumeAddress;
  private String resumePhoneNum;
  private String resumeEducation;
  private String resumeAbility;
  private String resumeAntecedents;
  private String resumeAwards;
  private String resumeContext;

  // 추가 필드들 (Users)
  private String userName;
  private LocalDate userBirth;
}
