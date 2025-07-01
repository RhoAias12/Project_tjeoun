package com.tjoeun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class MyPageController {
    @GetMapping("/apply_status")
    public String applyStatusPage() {
        return "mypage/apply_status";
    }

    @GetMapping("/scrap")
    public String scrapPage() {
        return "mypage/scrap";
    }
}
