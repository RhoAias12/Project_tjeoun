package com.tjoeun.elasticsearch.document;

import co.elastic.clients.json.JsonpDeserializable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "apply_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyHistoryDocument {

  @Id
  private Integer applyHistoryId;

  // 지원 상태
  private String status;
  private String statusDisplay;

  // 공고 관련
  private String recruitmentTitle;
  private String recruitmentCompany;
  private Long recruitmentId;
  private Long recruitmentDeadline;
  private String recruitmentLocation;
  private String recruitmentLogoUrl;

  // 지원자 정보
  private Integer userId;
  private String userEmail;
  private String userNickname;
  private String userName;
  private Long userBirth;

  // 이력서 정보
  private Long applyHistoryResumeId;
  private String resumeTitle;
  private String resumeImg;
  private String resumeAddress;
  private String resumePhoneNum;
  private String resumeEducation;
  private String resumeAbility;
  private String resumeAntecedents;
  private String resumeAwards;
  private String resumeContext;

  // 지원일시
  private Long appliedAt;
}