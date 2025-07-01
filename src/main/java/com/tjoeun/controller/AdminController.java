package com.tjoeun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/member_care")
    public String memberCarePage() {
        return "admin/member_care";
    }

    @GetMapping("/member_modify")
    public String memberModifyPage() {
        return "admin/member_modify";
    }

    @GetMapping("/manager_member_care")
    public String managerMemberCarePage() {
        return "admin/manager_member_care";
    }

    @GetMapping("/manager_list_care")
    public String managerListCarePage() {
        return "admin/manager_list_care";
    }

    @GetMapping("/resume_care")
    public String resumeCarePage() {
        return "admin/resume_care";
    }
}
