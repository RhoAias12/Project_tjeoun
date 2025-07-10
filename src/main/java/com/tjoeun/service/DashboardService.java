package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tjoeun.dto.DashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchService elasticsearchService;

    public void populateUserDashboard(Model model) {

        // 총 공고 수
        long totalJobCount = elasticsearchService.countRecruitments();
        model.addAttribute("totalJobCount", totalJobCount);

        // 마감 임박 공고 수 (예: 7일 이내 마감)
        long closingSoon = elasticsearchService.countClosedRecruitments();
        model.addAttribute("closingSoonCount", closingSoon);

        // 마감률 계산
        long closedCount = elasticsearchService.countClosedRecruitments();
        double closeRate = totalJobCount == 0 ? 0 : Math.round(((double) closedCount / totalJobCount) * 1000.0) / 10.0;
        model.addAttribute("closeRate", closeRate);

        // 인기 직무
        Map<String, Long> topRoles = elasticsearchService.getTopRoles(5);
        model.addAttribute("popularRoles", new ArrayList<>(topRoles.keySet()));
        model.addAttribute("popularRoleCounts", new ArrayList<>(topRoles.values()));

        // 키워드
        Map<String, Long> topKeywords = elasticsearchService.getTopKeywords(5);
        model.addAttribute("topKeywordLabels", new ArrayList<>(topKeywords.keySet()));
        model.addAttribute("topKeywordCounts", new ArrayList<>(topKeywords.values()));

        // 직무 비율
        model.addAttribute("jobTypeLabels", new ArrayList<>(topRoles.keySet()));
        model.addAttribute("jobTypeCounts", new ArrayList<>(topRoles.values()));
    }

    public void populateAdminDashboard(Model model) {
        long total = elasticsearchService.countRecruitments();
        long closed = elasticsearchService.countClosedRecruitments();
        long active = total - closed;

        model.addAttribute("totalJobs", total);
        model.addAttribute("activeJobs", active);
        model.addAttribute("closedJobs", closed);
        model.addAttribute("userCount", fetchUserCount());
        model.addAttribute("resumeRate", fetchResumeCompletionRate());
        model.addAttribute("crawlStatus", 98.3); // TODO: 필요시 시스템 상태 기준으로 연결

        // ✅ 관리자용 검색 키워드
        Map<String, Long> topKeywords = elasticsearchService.getTopKeywords(5);
        model.addAttribute("topSearchKeywords", new ArrayList<>(topKeywords.keySet()));
        model.addAttribute("topSearchCounts", new ArrayList<>(topKeywords.values()));
    }

    private long fetchUserCount() {
        // TODO: 실제 회원 수 DB 연동 필요
        return 1023;
    }

    private double fetchResumeCompletionRate() {
        // TODO: 이력서 작성률 DB 연동 필요
        return 72.5;
    }


}
