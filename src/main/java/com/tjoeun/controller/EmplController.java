package com.tjoeun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/empl")
public class EmplController {

    @GetMapping("/empl_main")
    public String emplMainPage() {
        return "empl/empl_main";
    }

    @GetMapping("/empl_detail")
    public String emplDetailPage() {
        return "empl/empl_detail";
    }


}
