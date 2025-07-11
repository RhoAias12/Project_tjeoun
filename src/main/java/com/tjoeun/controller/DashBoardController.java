package com.tjoeun.controller;

import com.tjoeun.dto.DashboardDTO;
import com.tjoeun.service.DashboardService;
import com.tjoeun.service.ElasticsearchService;
import com.tjoeun.service.RecruitmentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashBoardController {

    private final DashboardService dashboardService;
    private final ElasticsearchService elasticsearchService;
    private final RecruitmentSearchService recruitmentSearchService;


    @GetMapping("/user")
    public String userDashboard(Model model) {
        DashboardDTO dto = dashboardService.getUserDashboardData();
        model.addAttribute("dashboard", dto);
        return "dashboard/dashboard";

    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        DashboardDTO dto = dashboardService.getAdminDashboardData();
        model.addAttribute("dashboard", dto);
        return "dashboard/admin_dashboard";
    }

}
