package com.tjoeun.controller;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.service.FavoriteService;
import com.tjoeun.service.RecruitmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.tjoeun.util.PaginationUtil;


import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/empl")
public class EmplController {

    @Autowired
    private RecruitmentService recruitmentService;

    @Autowired
    private FavoriteService favoriteService;

    // 채용 공고 메인 페이지 (전체 리스트 출력 + 페이징 처리)
    @GetMapping("/empl_main")
    public String emplMainPage(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String customSort,
                               @PageableDefault(size = 25) Pageable pageable,
                               Model model) {

        int pageIndex = Math.max(0, page - 1);
        Pageable correctedPageable = PageRequest.of(page, pageable.getPageSize(), pageable.getSort());

        // 전체 리스트 전달
        List<RecruitmentDTO> jobList = recruitmentService.getAllPosts();
        model.addAttribute("jobList", jobList);

        //  정렬 기준에 따라 페이징 처리된 jobPage 생성
        Page<RecruitmentDTO> jobPage;
        if ("scrapOnly".equals(customSort)) {
            jobPage = recruitmentService.getOnlyFavorited(correctedPageable);
        } else if ("deadline".equals(customSort)) {
            jobPage = recruitmentService.getPostsSortedByDeadline(correctedPageable);
        } else {
            jobPage = recruitmentService.getPagedPosts(correctedPageable);
        }

        // jobPage 기준으로 페이징 정보 설정
        PaginationUtil.setPaging(model, jobPage, "/empl/empl_main");

        model.addAttribute("jobPage", jobPage);
        model.addAttribute("customSort", customSort);

        return "empl/empl_main";
    }



    // 개별 공고 상세 페이지
    @GetMapping("/empl_detail/{id}")
    public String emplDetailPage(@PathVariable("id") Long id, Model model, Principal principal) {
        RecruitmentDTO dto = recruitmentService.getPostById(id);

        boolean isFavorited = false;
        if (dto == null) {
            return "error/404"; // 404 페이지 (없으면 기본 페이지)
        }

        if (principal != null) {
            String userEmail = principal.getName();
            isFavorited = favoriteService.isFavorited(userEmail, id);
        }
        model.addAttribute("job", dto);
        model.addAttribute("isFavorited", isFavorited);

        return "empl/empl_detail";
    }

}
