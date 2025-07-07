package com.tjoeun.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDto {
  private String title;
  private String img;
  private String address;
  private String phoneNum;
  private String education;
  private String ability;
  private String antecedents;
  private String awards;
  private String context;

  private MultipartFile imgFile;



  // 새로 추가
  private List<ResumeContentDTO> resumeContents;
}