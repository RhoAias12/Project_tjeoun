package com.tjoeun.controller;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.service.RecruitmentService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/empl")
public class EmplController {

    @Autowired
    private RecruitmentService recruitmentService;

    // 채용 공고 메인 페이지 (전체 리스트 출력)
    @GetMapping("/empl_main")
    public String emplMainPage(@PageableDefault(size = 25) Pageable pageable, Model model) {

        List<RecruitmentDTO> jobList = recruitmentService.getAllPosts();

        Page<RecruitmentDTO> jobPage = recruitmentService.getPagedPosts(pageable);

        int currentPage = jobPage.getNumber(); // 현재 페이지 (0부터 시작)
        int totalPages = jobPage.getTotalPages();

        int startPage = (currentPage / 10) * 10;
        int endPage = Math.min(startPage + 9, totalPages - 1); // 페이지 번호는 0부터 시작

        model.addAttribute("jobPage", jobPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
//        model.addAttribute("jobList", jobList);

        return "empl/empl_main";
    }


    // 개별 공고 상세 페이지
    @GetMapping("/empl_detail/{id}")
    public String emplDetailPage(@PathVariable("id") Long id, Model model) {
        RecruitmentDTO dto = recruitmentService.getPostById(id);
        if (dto == null) {
            return "error/404"; // 404 페이지 (없으면 기본 페이지)
        }
        model.addAttribute("job", dto);
        return "empl/empl_detail";
    }

}
