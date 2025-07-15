package com.tjoeun.controller;

import com.tjoeun.dto.DashboardDTO;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.UserRepository;
import com.tjoeun.service.DashboardService;
import com.tjoeun.elasticsearch.service.ElasticsearchService;
import com.tjoeun.elasticsearch.service.RecruitmentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

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

        return "dashboard/dashboard";
    }


    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        DashboardDTO dashboard = dashboardService.getAdminDashboardData(); // null 필요 없으면 파라미터 없애도 됨
        model.addAttribute("dashboard", dashboard);

        return "dashboard/ad_dashboard";
    }


}
