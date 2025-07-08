package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyHistoryResumeDTO {
  private Long id;
  private String title;
  private String img;
  private String address;
  private String phoneNum;
  private String education;
  private String ability;
  private String antecedents;
  private String awards;
  private String context;

  private List<ApplyHistoryResumeContentDTO> resumeContents;

  private Timestamp createdAt;
  private Timestamp updatedAt;

  private UserListDto user;
}
