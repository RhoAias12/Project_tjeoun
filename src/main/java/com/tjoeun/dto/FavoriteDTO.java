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
  private Integer scrapCount;
  private boolean isApplied;

  public void setScrapCount(Integer scrapCount) {
    this.scrapCount = (scrapCount == null) ? 0 : scrapCount;
  }
}