package com.tjoeun.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeDto {

  private String title;
  private String context;
  private String address;
  private String phoneNum;
  private String education;
  private String ability;
  private String antecedents;
  private String awards;
  private String img;

  // 생성자, getter, setter는 lombok으로 자동 생성
}