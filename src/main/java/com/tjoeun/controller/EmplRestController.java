package com.tjoeun.controller;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.service.EmplService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empl")
@RequiredArgsConstructor
public class EmplRestController {

  private final EmplService emplService;

  // 채용공고 리스트 JSON 반환
  @GetMapping("/list")
  public List<RecruitmentDTO> getRecruitments() {
    return emplService.getJobPage(
      1, 25, "all", null, null, null, null, null, null
    ).getContent();
  }

  // 공고 상세 JSON 반환 (필요하면)
  @GetMapping("/{id}")
  public RecruitmentDTO getRecruitment(@PathVariable Long id) {
    return emplService.getRecruitmentDetail(id);
  }
}
