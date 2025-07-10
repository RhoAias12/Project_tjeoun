package com.tjoeun.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListDto {
  private Integer userIdx;
  private String userName;
  private String userEmail;
  private LocalDate userBirth;
  private String userNickname;
}
