package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tjoeun.dto.DashboardDTO;
import com.tjoeun.elasticsearch.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchService elasticsearchService;
    private final SearchLogRepository searchLogRepository;

    private double calculateCloseRate(long totalJobCount, long closedCount) {
        if (totalJobCount == 0) return 0.0;
        return Math.round(((double) closedCount / totalJobCount) * 1000.0) / 10.0;
    }

    public DashboardDTO getUserDashboardData() {
        long total = elasticsearchService.countRecruitments();
        long closed = elasticsearchService.countClosedRecruitments();
        long closingSoon = elasticsearchService.countClosingSoonRecruitments();
        double closeRate = calculateCloseRate(total, closed);

        Map<String, Long> topRoles = elasticsearchService.getTopRoles(5);
        Map<String, Long> topSearchKeywords = getTopSearchKeywords();

        System.out.println("✅ topRoles = " + topRoles);
        System.out.println("🔍 topSearchKeywords = " + topSearchKeywords);

        return DashboardDTO.builder()
                .totalJobCount(total)
                .closingSoonCount(closingSoon)
                .closedCount(closed)
                .closeRate(closeRate)
                .popularRoles(new ArrayList<>(topRoles.keySet()))
                .popularRoleCounts(new ArrayList<>(topRoles.values()))
                .topKeywordLabels(new ArrayList<>(topSearchKeywords.keySet()))
                .topKeywordCounts(new ArrayList<>(topSearchKeywords.values()))
                .jobTypeLabels(new ArrayList<>(topRoles.keySet()))
                .jobTypeCounts(new ArrayList<>(topRoles.values()))
                .build();
    }

    public DashboardDTO getAdminDashboardData() {
        long total = elasticsearchService.countRecruitments();
        long closed = elasticsearchService.countClosedRecruitments();
        long active = total - closed;
        double closeRate = calculateCloseRate(total, closed);

        Map<String, Long> topSearchKeywords = getTopSearchKeywords();

        return DashboardDTO.builder()
                .totalJobCount(total)
                .closedCount(closed)
                .closeRate(closeRate)
//                .userCount(fetchUserCount())
//                .resumeRate(fetchResumeCompletionRate())
                .crawlStatus(98.3)
//                .topKeywordLabels(new ArrayList<>(topKeywords.keySet()))
//                .topKeywordCounts(new ArrayList<>(topKeywords.values()))
                .build();
    }

    private Map<String, Long> getTopSearchKeywords() {
        List<Object[]> result = searchLogRepository.findTopKeywords();
        return result.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> ((Number) r[1]).longValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


}
