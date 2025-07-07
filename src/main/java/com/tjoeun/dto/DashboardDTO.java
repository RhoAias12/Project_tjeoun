package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private int totalJobCount;
    private List<String> popularJobs;
    private List<String> popularStacks;
    private Map<String, Integer> regionDistribution;
    private Map<String, Integer> jobTrendsByDate;
    private int closingSoonCount;
}
