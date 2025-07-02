package com.tjoeun.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDTO {

  private Integer favoriteIdx;
  private Integer userIdx;
  private Long recruitmentIdx;
  private String title;
  private String company;
  private LocalDate deadline;
}