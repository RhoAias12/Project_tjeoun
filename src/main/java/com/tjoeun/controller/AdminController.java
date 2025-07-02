package com.tjoeun.controller;


import com.tjoeun.service.AdminService;
import com.tjoeun.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/member_list")
    public String memberList(Model model) {
        List<UserListDto> userList = adminService.getAllUsers();
        model.addAttribute("userList", userList);
        return "admin/member_list";
    }
    @PostMapping("/member_list/delete")
    public String deleteMember(@RequestParam("userIdx") Integer userIdx) {
        adminService.deleteUserById(userIdx);
        return "redirect:/admin/member_list";
    }


    @GetMapping("/member_modify")
    public String memberModify() {
        return "admin/ad_member_modify";
    }

    @GetMapping("/recruit_list")
    public String recruitList() {
        return "admin/recruit_list";
    }

    @GetMapping("/recruit_modify")
    public String recruitModify() {
        return "admin/recruit_modify";
    }
}
