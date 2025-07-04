package com.tjoeun.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ApplyDetailDTO {
  private String recruitmentTitle;
  private String recruitmentCompany;
  private LocalDate recruitmentDeadline;
  private String recruitmentLocation;
  private String recruitmentLogoUrl;

  private String resumeTitle;
  private String resumeImg;
  private String resumeAddress;
  private String resumePhoneNum;
  private String resumeEducation;
  private String resumeAbility;
  private String resumeAntecedents;
  private String resumeAwards;
  private String resumeContext;
  private List<ResumeContentDTO> resumeContentList;

  private String userName;
  private String userEmail;
  private LocalDate userBirth;
}
