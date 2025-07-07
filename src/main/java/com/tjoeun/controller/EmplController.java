package com.tjoeun.controller;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.service.FavoriteService;
import com.tjoeun.service.RecruitmentService;
import com.tjoeun.util.PaginationUtil2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/empl")
public class EmplController {

    @Autowired
    private RecruitmentService recruitmentService;

    @Autowired
    private FavoriteService favoriteService;

    // 채용 공고 메인 페이지 (전체 리스트 출력 + 페이징 처리)
    @GetMapping("/empl_main")
    public String emplMainPage(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(required = false) String deadlineSort,
                               @RequestParam(required = false) String scrapSort,
                               @PageableDefault(size = 25) Pageable pageable,
                               Model model) {

        int pageSize = pageable.getPageSize();
        boolean usingScrapSort = scrapSort != null && !scrapSort.isEmpty();

        if (usingScrapSort) {
            // ✅ 1. 전체 가져오기 + 스크랩 개수 정렬
            List<RecruitmentDTO> all = recruitmentService.getAllPosts();

            // 🔹 스크랩 개수 기준 정렬
            if ("scrap-desc".equals(scrapSort)) {
                all.sort(Comparator.comparing(RecruitmentDTO::getScrapCount, Comparator.nullsFirst(Integer::compareTo)).reversed());
            } else if ("scrap-asc".equals(scrapSort)) {
                all.sort(Comparator.comparing(RecruitmentDTO::getScrapCount, Comparator.nullsFirst(Integer::compareTo)));
            }

            // 🔹 추가 정렬: 마감일
            if ("deadline-asc".equals(deadlineSort)) {
                all.sort(Comparator.comparing(RecruitmentDTO::getDeadline));
            } else if ("deadline-desc".equals(deadlineSort)) {
                all.sort(Comparator.comparing(RecruitmentDTO::getDeadline).reversed());
            }

            // 🔹 메모리 페이징 처리
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, all.size());
            List<RecruitmentDTO> pageContent = all.subList(start, end);

            Page<RecruitmentDTO> jobPage = new PageImpl<>(pageContent, PageRequest.of(page - 1, pageSize), all.size());

            model.addAttribute("jobList", pageContent);
            model.addAttribute("jobPage", jobPage);

            // URL 유지: scrapSort와 deadlineSort를 함께 넘김
            PaginationUtil2.setPaging(model, jobPage, "/empl/empl_main", scrapSort, deadlineSort, 10);


        } else {
            // ✅ scrapSort 없으면 → DB 정렬 + 페이징
            String combinedSort = (deadlineSort != null && !deadlineSort.isEmpty()) ? deadlineSort : null;

            Pageable correctedPageable = PageRequest.of(page - 1, pageSize, pageable.getSort());
            Page<RecruitmentDTO> jobPage = recruitmentService.getPagedPosts(correctedPageable, combinedSort);

            model.addAttribute("jobList", jobPage.getContent());
            model.addAttribute("jobPage", jobPage);

            PaginationUtil2.setPaging(model, jobPage, "/empl/empl_main", scrapSort, deadlineSort, 10);

        }

        // ✅ 드롭다운 유지
        model.addAttribute("scrapSort", scrapSort);
        model.addAttribute("deadlineSort", deadlineSort);

        return "empl/empl_main";
    }




    // 개별 공고 상세 페이지
    @GetMapping("/empl_detail/{id}")
    public String emplDetailPage(@PathVariable("id") Long id,
                                 @RequestParam(defaultValue = "1") int page,
                                 Model model, Principal principal) {
        RecruitmentDTO dto = recruitmentService.getPostById(id);

        boolean isFavorited = false;
        if (dto == null) {
            return "error/404"; // 404 페이지 (없으면 기본 페이지)
        }

        if (principal != null) {
            String userEmail = principal.getName();
            isFavorited = favoriteService.isFavorited(userEmail, id);
        }

        model.addAttribute("page", page);
        model.addAttribute("job", dto);
        model.addAttribute("isFavorited", isFavorited);

        return "empl/empl_detail";
    }

}
