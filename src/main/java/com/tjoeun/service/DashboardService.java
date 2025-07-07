package com.tjoeun.service;

import com.tjoeun.dto.DashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    public DashboardDTO getDashboardData() {
        DashboardDTO dto = new DashboardDTO();

        // 총 공고 수
        dto.setTotalJobCount(35214);

        // 인기 직무 TOP 5
        dto.setPopularJobs(Arrays.asList("백엔드", "프론트엔드", "데이터 분석", "AI 엔지니어", "DevOps"));

        // 인기 기술 스택
        dto.setPopularStacks(Arrays.asList("Java", "Spring Boot", "React", "Python", "Docker"));

        // 지역별 공고 수
        Map<String, Integer> regionStats = new LinkedHashMap<>();
        regionStats.put("서울", 18000);
        regionStats.put("경기", 10000);
        regionStats.put("부산", 4200);
        regionStats.put("대전", 1500);
        regionStats.put("기타", 1514);
        dto.setRegionDistribution(regionStats);

        // 최근 한 달간 공고 수 (날짜별)
        Map<String, Integer> monthlyTrend = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            String date = now.minusDays(i).toString();
            monthlyTrend.put(date, new Random().nextInt(200) + 100); // 100~299 랜덤
        }
        dto.setRegionDistribution(monthlyTrend);

        // 마감 임박 공고 수 (오늘 포함 3일 이내)
        dto.setClosingSoonCount(874);

        return dto;
    }
}
