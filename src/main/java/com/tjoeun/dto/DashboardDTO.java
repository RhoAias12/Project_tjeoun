package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class DashboardDTO {
    private long totalJobCount;
    private long closingSoonCount;
    private long closedCount;
    private double closeRate;

    private List<String> popularRoles;
    private List<Long> popularRoleCounts;

    private List<String> topKeywordLabels;
    private List<Long> topKeywordCounts;

    private List<String> jobTypeLabels;
    private List<Long> jobTypeCounts;

    private List<String> regionLabels;
    private List<Long> regionCounts;

    private int myApplyCount;
    private double avgApplyCount;

    private List<String> applyTrendLabels;
    private List<Long> applyTrendCounts;

    // 관리자
    private long totalUserCount;  // 사용자 수
    private long totalApplyCount; // 지원 수

    private List<String> ageGroupLabels;
    private List<Long> ageGroupCounts;


}
