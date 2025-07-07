// AdminDashboardController.java
package com.tjoeun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping("/ad_dashboard")
    public String showAdminDashboard(Model model) {
        // Mock 데이터
        model.addAttribute("totalJobs", 1234);
        model.addAttribute("activeJobs", 789);
        model.addAttribute("closedJobs", 445);
        model.addAttribute("userCount", 2000);
        model.addAttribute("resumeRate", 62.5);
        model.addAttribute("crawlStatus", 97.3);

        model.addAttribute("topSearchKeywords", List.of("백엔드", "AI", "재택", "React", "Spring"));
        model.addAttribute("topSearchCounts", List.of(150, 120, 100, 80, 60));



        return "admin/ad_dashboard";
    }
}
