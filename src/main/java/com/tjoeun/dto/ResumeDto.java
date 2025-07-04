package com.tjoeun.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class ResumeDto {

  private String title;         // 제목
  private String img;           // 이미지 경로 (또는 업로드 파일명)
  private String address;       // 주소
  private String phoneNum;      // 전화번호
  private String education;     // 학력
  private String ability;       // 자격증 및 스킬
  private String antecedents;   // 경력
  private String awards;        // 수상이력
  private String context;       // 자기소개 또는 추가내용
  private MultipartFile imgFile;
  private LocalDate userBirth;

  // 질문-답변 1~4
  private String question1;
  private String answer1;

  private String question2;
  private String answer2;

  private String question3;
  private String answer3;

  private String question4;
  private String answer4;

  // 필요시 user_idx는 로그인 사용자로 처리하므로 DTO에 안넣어도 됨
}