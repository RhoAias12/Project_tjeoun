package com.tjoeun.controller;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.UserRepository;
import com.tjoeun.service.EmplService;
import com.tjoeun.service.MyPageService;
import com.tjoeun.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/empl")
public class EmplController {

  private final EmplService emplService;
  private final MyPageService myPageService;
  private final UserRepository userRepository;

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
      Principal principal,
      Model model) {

        Integer userIdx = null;
        if (principal != null) {
            String userEmail = principal.getName();
            Users user = userRepository.findByUserEmail(userEmail);
            if (user != null) {
                userIdx = user.getUserIdx();
            }
        }

        Page<RecruitmentDTO> jobPage = emplService.getJobPage(
          page, pageSize, sortOrder,
          title, content, region, company, startDate, endDate, userIdx);

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
        return "/main";
      }

      boolean isFavorited = false;
      if (principal != null) {
        String userEmail = principal.getName();
        isFavorited = myPageService.isFavorited(userEmail, id);
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
}

