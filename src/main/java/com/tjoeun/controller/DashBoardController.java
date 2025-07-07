package com.tjoeun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashBoardController {

    @GetMapping
    public String showDashboard(Model model) {
        // 총 공고 수
        model.addAttribute("totalJobCount", 35214);

        // 인기 직무
        model.addAttribute("topJobs", List.of(
                Map.of("name", "백엔드", "count", 8342),
                Map.of("name", "프론트엔드", "count", 7321),
                Map.of("name", "데이터 분석", "count", 5210),
                Map.of("name", "AI 엔지니어", "count", 4010),
                Map.of("name", "기획자", "count", 3130)
        ));

        // 인기 기술 스택
        model.addAttribute("popularStacks", List.of("Java", "React", "Spring Boot", "Python", "JavaScript"));

        // 지역 분포
        model.addAttribute("regionDistribution", Map.of(
                "서울", 14200,
                "경기", 8700,
                "부산", 3200,
                "대구", 1500,
                "기타", 2614
        ));

        // 공고 트렌드 원본 데이터
        List<Map<String, Object>> trends = List.of(
                Map.of("date", "06-01", "count", 400),
                Map.of("date", "06-02", "count", 450),
                Map.of("date", "06-03", "count", 510),
                Map.of("date", "06-04", "count", 600),
                Map.of("date", "06-05", "count", 550)
        );
        model.addAttribute("recentTrends", trends);

        // 👇 별도로 JavaScript에 쓸 배열만 추출
        List<String> trendLabels = trends.stream()
                .map(t -> (String) t.get("date"))
                .toList();

        List<Integer> trendData = trends.stream()
                .map(t -> (Integer) t.get("count"))
                .toList();

        model.addAttribute("trendLabels", trendLabels);
        model.addAttribute("trendData", trendData);

        // 마감 임박 수
        model.addAttribute("closingSoonCount", 874);

        return "dashboard/dash";
    }

}
