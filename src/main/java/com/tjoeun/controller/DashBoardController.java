package com.tjoeun.controller;

import com.tjoeun.dto.DashboardDTO;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.UserRepository;
import com.tjoeun.service.DashboardService;
import com.tjoeun.service.ElasticsearchService;
import com.tjoeun.service.RecruitmentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashBoardController {

    private final DashboardService dashboardService;
    private final ElasticsearchService elasticsearchService;
    private final RecruitmentSearchService recruitmentSearchService;
    private final UserRepository userRepository;

    @GetMapping("/user")
    public String userDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // 혹은 로그인 페이지
        }

        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);
        if (user == null) {
            return "error/404"; // 또는 예외 처리
        }

        DashboardDTO dto = dashboardService.getUserDashboardData(user.getUserIdx().longValue());
        model.addAttribute("dashboard", dto);

        List<Map.Entry<String, Long>> keywordTop5 = elasticsearchService.getTopKeywords(5).entrySet().stream()
                .collect(Collectors.toList());
        model.addAttribute("keywordTop5", keywordTop5);

        return "dashboard/dashboard";
    }


    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        DashboardDTO dto = dashboardService.getAdminDashboardData();
        model.addAttribute("dashboard", dto);
        return "dashboard/admin_dashboard";
    }

}
