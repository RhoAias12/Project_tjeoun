package com.tjoeun.ES_controller;

import com.tjoeun.ES_service.RecruitmentSearchService;
import com.tjoeun.document.RecruitmentElastic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final RecruitmentSearchService searchService;

    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        List<RecruitmentElastic> results = searchService.search(keyword);
        model.addAttribute("results", results);
        return "search_result"; // Thymeleaf 결과 페이지
    }
}
