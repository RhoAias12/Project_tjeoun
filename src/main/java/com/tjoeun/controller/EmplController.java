package com.tjoeun.controller;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.service.EmplService;
import com.tjoeun.service.FavoriteService;
import com.tjoeun.util.PaginationUtil2;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/empl")
public class EmplController {

    private final EmplService emplService;
    private final FavoriteService favoriteService;

    @GetMapping("/empl_main")
    public String emplMainPage(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String deadlineSort,
      @RequestParam(required = false) String scrapSort,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String content,
      @RequestParam(required = false) String region,
      @RequestParam(required = false) String company,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      Model model) {

        Page<RecruitmentDTO> jobPage = emplService.getJobPage(
          page,
          25, // 기본 페이지 크기
          scrapSort,
          deadlineSort,
          title,
          content,
          region,
          company,
          startDate,
          endDate);

        String prevUrl = buildPrevUrl(page, scrapSort, deadlineSort, title, content, region, company, startDate, endDate);
        String encodedPrevUrl = URLEncoder.encode(prevUrl, StandardCharsets.UTF_8);

        model.addAttribute("prevUrl", prevUrl);
        model.addAttribute("encodedPrevUrl", encodedPrevUrl);
        model.addAttribute("jobList", jobPage.getContent());
        model.addAttribute("jobPage", jobPage);

        PaginationUtil2.setPaging(model, jobPage, "/empl/empl_main",
          scrapSort, deadlineSort, 10,
          title, content, region, company, startDate, endDate);

        model.addAttribute("scrapSort", scrapSort);
        model.addAttribute("deadlineSort", deadlineSort);
        model.addAttribute("title", title);
        model.addAttribute("content", content);
        model.addAttribute("region", region);
        model.addAttribute("company", company);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "empl/empl_main";
    }

    @GetMapping("/empl_detail/{id}")
    public String emplDetailPage(@PathVariable("id") Long id,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(required = false) String prevUrl,
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

        return "empl/empl_detail";
    }

    // URL 빌더 (서비스로 분리 가능)
    private String buildPrevUrl(int page, String scrapSort, String deadlineSort,
                                String title, String content, String region, String company,
                                String startDate, String endDate) {
        StringBuilder sb = new StringBuilder("/empl/empl_main?page=").append(page);
        if (scrapSort != null && !scrapSort.isEmpty()) sb.append("&scrapSort=").append(URLEncoder.encode(scrapSort, StandardCharsets.UTF_8));
        if (deadlineSort != null && !deadlineSort.isEmpty()) sb.append("&deadlineSort=").append(URLEncoder.encode(deadlineSort, StandardCharsets.UTF_8));
        if (title != null && !title.isEmpty()) sb.append("&title=").append(URLEncoder.encode(title, StandardCharsets.UTF_8));
        if (content != null && !content.isEmpty()) sb.append("&content=").append(URLEncoder.encode(content, StandardCharsets.UTF_8));
        if (region != null && !region.isEmpty()) sb.append("&region=").append(URLEncoder.encode(region, StandardCharsets.UTF_8));
        if (company != null && !company.isEmpty()) sb.append("&company=").append(URLEncoder.encode(company, StandardCharsets.UTF_8));
        if (startDate != null && !startDate.isEmpty()) sb.append("&startDate=").append(URLEncoder.encode(startDate, StandardCharsets.UTF_8));
        if (endDate != null && !endDate.isEmpty()) sb.append("&endDate=").append(URLEncoder.encode(endDate, StandardCharsets.UTF_8));
        return sb.toString();
    }
}
