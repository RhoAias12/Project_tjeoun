package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tjoeun.dto.DashboardDTO;
import com.tjoeun.elasticsearch.repository.SearchLogRepository;
import com.tjoeun.repository.ApplyHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    private final ApplyHistoryRepository applyHistoryRepository;

    private double calculateCloseRate(long totalJobCount, long closedCount) {
        if (totalJobCount == 0) return 0.0;
        return Math.round(((double) closedCount / totalJobCount) * 1000.0) / 10.0;
    }

    public DashboardDTO getUserDashboardData(Long userIdx) {
        int myApplyCount = applyHistoryRepository.countByUserIdx(userIdx);

        Double avgApplyCount = applyHistoryRepository.findAvgApplyCountPerUser();
        if (avgApplyCount == null) avgApplyCount = 0.0;

        long total = elasticsearchService.countRecruitments();
        long closed = elasticsearchService.countClosedRecruitments();
        long closingSoon = elasticsearchService.countClosingSoonRecruitments();
        double closeRate = calculateCloseRate(total, closed);

        Map<String, Long> topRoles = elasticsearchService.getTopRoles(5);
        List<Map.Entry<String, Long>> topSearchKeywords = getTopSearchedKeywords(5);
        Map<String, Long> regionData = elasticsearchService.getRegionDistribution();

        System.out.println("✅ topRoles = " + topRoles);
        System.out.println("🔍 topSearchKeywords = " + topSearchKeywords);


        return DashboardDTO.builder()
                .totalJobCount(total)
                .closingSoonCount(closingSoon)
                .closedCount(closed)
                .closeRate(closeRate)

                .myApplyCount(myApplyCount)
                .avgApplyCount(avgApplyCount)

                // 인기 직무
                .popularRoles(new ArrayList<>(topRoles.keySet()))
                .popularRoleCounts(new ArrayList<>(topRoles.values()))

                // 인기 키워드
                .topKeywordLabels(topSearchKeywords.stream().map(Map.Entry::getKey).toList())
                .topKeywordCounts(topSearchKeywords.stream().map(Map.Entry::getValue).toList())

                // 지역 분포
                .regionLabels(new ArrayList<>(regionData.keySet()))
                .regionCounts(new ArrayList<>(regionData.values()))
                .build();
    }

    public DashboardDTO getAdminDashboardData() {
        long total = elasticsearchService.countRecruitments();
        long closed = elasticsearchService.countClosedRecruitments();
        long active = total - closed;
        double closeRate = calculateCloseRate(total, closed);

//        Map<String, Long> topSearchKeywords = topSearchKeywords();

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

    public List<Map.Entry<String, Long>> getTopSearchedKeywords(int size) {
        List<Object[]> result = searchLogRepository.findTopKeywords(PageRequest.of(0, size));
        return result.stream()
                .map(row -> Map.entry((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }



}
