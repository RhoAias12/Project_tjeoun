package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

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

    // 관리자용 전용 필드
    private long userCount;
    private double resumeRate;
    private double crawlStatus;
}
