package com.tjoeun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/react")
public class ReactController {
    @GetMapping("/react_write")
    public String reactWritePage() {
        return "react/react_write";
    }

    @GetMapping("/react_modify")
    public String reactModifyPage() {
        return "react/react_modify";
    }

    @GetMapping("/react_detail")
    public String reactDetailPage() {
        return "react/react_detail";
    }
}
