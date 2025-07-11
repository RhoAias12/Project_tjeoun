package com.tjoeun.controller;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.service.EmplService;
import com.tjoeun.service.FavoriteService;
import com.tjoeun.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/empl")
public class EmplController {

    private final EmplService emplService;
    private final FavoriteService favoriteService;

    @GetMapping("/empl_main")
    public String emplMainPage(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "25") int pageSize,
      @RequestParam(defaultValue = "all") String sortOrder,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String content,
      @RequestParam(required = false) String region,
      @RequestParam(required = false) String company,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      Model model) {

        Page<RecruitmentDTO> jobPage = emplService.getJobPage(
          page, pageSize, sortOrder,
          title, content, region, company, startDate, endDate);

        String prevUrl = emplService.buildPrevUrl(
          page, pageSize, sortOrder,
          title, content, region, company, startDate, endDate);

        String encodedPrevUrl = URLEncoder.encode(prevUrl, StandardCharsets.UTF_8);

        model.addAttribute("jobList", jobPage.getContent());
        model.addAttribute("jobPage", jobPage);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("prevUrl", prevUrl);
        model.addAttribute("encodedPrevUrl", encodedPrevUrl);

        model.addAttribute("title", title);
        model.addAttribute("content", content);
        model.addAttribute("region", region);
        model.addAttribute("company", company);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        PaginationUtil.setPaging(model, jobPage, "/empl/empl_main",
          sortOrder, 10,
          title, content, region, company, startDate, endDate);

        return "empl/empl_main";
    }

    @GetMapping("/empl_detail/{id}")
    public String emplDetailPage(
      @PathVariable("id") Long id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String prevUrl,
      @RequestParam(required = false) String sortOrder,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String content,
      @RequestParam(required = false) String region,
      @RequestParam(required = false) String company,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      Model model,
      Principal principal) {

        RecruitmentDTO dto = emplService.getRecruitmentDetail(id);
        if (dto == null) {
            return "error/404";
        }

        boolean isFavorited = false;
        if (principal != null) {
            String userEmail = principal.getName();
            isFavorited = favoriteService.isFavorited(userEmail, id);
        }

        model.addAttribute("page", page);
        model.addAttribute("job", dto);
        model.addAttribute("prevUrl", prevUrl != null ? prevUrl : "/empl/empl_main");
        model.addAttribute("isFavorited", isFavorited);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("title", title);
        model.addAttribute("content", content);
        model.addAttribute("region", region);
        model.addAttribute("company", company);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "empl/empl_detail";
    }

    // 아래는 삭제! 서버 렌더링이면 불필요!
    // @GetMapping("/{id}") ...
    // @GetMapping("/list") ...
}