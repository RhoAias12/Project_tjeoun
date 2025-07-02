package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserListDto {
  private Integer userIdx;
  private String userEmail;
  private String userNickname;
}
