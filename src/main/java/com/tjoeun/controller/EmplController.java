package com.tjoeun.controller;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.service.EmplService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/empl")
@RequiredArgsConstructor
public class EmplController {

  private static final Logger log = LoggerFactory.getLogger(EmplController.class);

  private final EmplService emplService;

  // ✅ 채용공고 리스트 JSON 반환
  @GetMapping("/list")
  public ResponseEntity<List<RecruitmentDTO>> getRecruitments() {
    try {
      List<RecruitmentDTO> list = emplService.getJobPage(
        1, 25, "all", null, null, null, null, null, null
      ).getContent();
      return ResponseEntity.ok(list);
    } catch (Exception e) {
      log.error("채용공고 리스트 조회 실패", e);
      // 500 Internal Server Error + 빈 리스트 반환
      return ResponseEntity.internalServerError().body(Collections.emptyList());
    }
  }

  // ✅ 공고 상세 JSON 반환
  @GetMapping("/{id}")
  public ResponseEntity<RecruitmentDTO> getRecruitment(@PathVariable Long id) {
    RecruitmentDTO dto = emplService.getRecruitmentDetail(id);
    if (dto == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(dto);
  }
}