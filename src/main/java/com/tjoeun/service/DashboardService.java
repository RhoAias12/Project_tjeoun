package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tjoeun.constant.UserRole;
import com.tjoeun.dto.DashboardDTO;
import com.tjoeun.elasticsearch.repository.SearchLogRepository;
import com.tjoeun.elasticsearch.service.ElasticsearchService;
import com.tjoeun.repository.ApplyHistoryRepository;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchService elasticsearchService;
    private final SearchLogRepository searchLogRepository;
    private final ApplyHistoryRepository applyHistoryRepository;
    private final UserRepository userRepository;

    private double calculateCloseRate(long totalJobCount, long closedCount) {
        if (totalJobCount == 0) return 0.0;
        return Math.round(((double) closedCount / totalJobCount) * 1000.0) / 10.0;
    }

    public DashboardDTO getUserDashboardData(Long userIdx) {
        int myApplyCount = applyHistoryRepository.countByUserIdx(userIdx);

        List<Long> counts = applyHistoryRepository.findApplyCountsPerUser();
        double avgApplyCount = counts.stream().mapToLong(Long::longValue).average().orElse(0.0);

        long total = elasticsearchService.countRecruitments();
        long closed = elasticsearchService.countClosedRecruitments();
        long closingSoon = elasticsearchService.countClosingSoonRecruitments();
        double closeRate = calculateCloseRate(total, closed);

        Map<String, Long> topRoles = elasticsearchService.getTopRoles(5);
        List<Map.Entry<String, Long>> topSearchKeywords = getTopSearchedKeywords(5);
        Map<String, Long> regionData = elasticsearchService.getRegionDistribution();

        Map<String, Long> applyTrend = elasticsearchService.getUserWeeklyApplyTrend(userIdx, applyHistoryRepository);
        List<String> applyTrendLabels = new ArrayList<>(applyTrend.keySet());
        List<Long> applyTrendCounts = new ArrayList<>(applyTrend.values());


        System.out.println("topRoles = " + topRoles);
        System.out.println("topSearchKeywords = " + topSearchKeywords);


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

                .applyTrendLabels(applyTrendLabels)
                .applyTrendCounts(applyTrendCounts)

                .build();
    }

    public DashboardDTO getAdminDashboardData() {
        // 전체 지원 수
        long totalApplyCount = applyHistoryRepository.count();

        // 전체 사용자 수
        long totalUserCount = userRepository.count();

        // 전체 공고 수 등 기존 로직 재활용
        long totalJobCount = elasticsearchService.countRecruitments();
        long closedCount = elasticsearchService.countClosedRecruitments();
        long closingSoonCount = elasticsearchService.countClosingSoonRecruitments();
        double closeRate = calculateCloseRate(totalJobCount, closedCount);

        // 인기 직무 (엘라스틱)
        Map<String, Long> topRoles = elasticsearchService.getTopRoles(5);

        // 검색 키워드 (최근 7일 기준)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> recentKeywords = searchLogRepository.findTopKeywordsAfterDate(weekAgo, PageRequest.of(0, 5));
        List<Map.Entry<String, Long>> topSearchKeywords = recentKeywords.stream()
                .map(row -> Map.entry((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        Map<String, Long> ageGroups = userRepository.findAll().stream()
                .filter(user -> user.getUserRole() == UserRole.USER)
                .map(user -> {
                    int age = LocalDate.now().getYear() - user.getUserBirth().getYear();
                    if (age < 20) return "10대 이하";
                    else if (age < 30) return "20대";
                    else if (age < 40) return "30대";
                    else if (age < 50) return "40대";
                    else if (age < 60) return "50대";
                    else return "60대 이상";
                })
                .collect(Collectors.groupingBy(ageGroup -> ageGroup, Collectors.counting()));

        List<String> ageGroupLabels = new ArrayList<>(ageGroups.keySet());
        List<Long> ageGroupCounts = new ArrayList<>(ageGroups.values());

        return DashboardDTO.builder()
                .totalUserCount(totalUserCount)
                .totalJobCount(totalJobCount)
                .totalApplyCount(totalApplyCount)
                .closingSoonCount(closingSoonCount)
                .closedCount(closedCount)
                .closeRate(closeRate)

                .popularRoles(new ArrayList<>(topRoles.keySet()))
                .popularRoleCounts(new ArrayList<>(topRoles.values()))

                .topKeywordLabels(topSearchKeywords.stream().map(Map.Entry::getKey).toList())
                .topKeywordCounts(topSearchKeywords.stream().map(Map.Entry::getValue).toList())

                .ageGroupLabels(ageGroupLabels)
                .ageGroupCounts(ageGroupCounts)

                .build();
    }


    public List<Map.Entry<String, Long>> getTopSearchedKeywords(int size) {
        List<Object[]> result = searchLogRepository.findTopKeywords(PageRequest.of(0, size));
        return result.stream()
                .map(row -> Map.entry((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }



}