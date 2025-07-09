package com.tjoeun.controller;

import com.tjoeun.service.DashboardService;
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

    @GetMapping("/user")
    public String userDashboard(Model model) {
        dashboardService.populateUserDashboard(model);
        return "dashboard/dashboard";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        dashboardService.populateAdminDashboard(model);
        return "dashboard/ad_dashboard";
    }


}
