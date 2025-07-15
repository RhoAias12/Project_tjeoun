package com.tjoeun.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDto {

  @Size(max = 60, message = "제목은 최대 60자까지 입력할 수 있습니다.")
  private String title;
  private String context;
  private String img;
  private String address;
  private String phoneNum;
  private String education;
  private String antecedents;
  private String ability;
  private String awards;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  private List<ResumeContentDTO> resumeContents;
}
