package com.tjoeun.service;

import com.tjoeun.constant.UserRole;
import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.RecruitmentRepository;
import com.tjoeun.repository.UserRepository;
import com.tjoeun.specification.RecruitmentSpecification;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EmplService {

  private final RecruitmentRepository recruitmentRepository;
  private final RecruitmentSearchService recruitmentSearchService;
  private final UserRepository userRepository;

  public Page<RecruitmentDTO> getJobPage(
    int page,
    int pageSize,
    String sortOrder,
    String title,
    String content,
    String region,
    String company,
    String startDateStr,
    String endDateStr,
    Integer userIdx) {

    // [1] 검색 키워드 존재 시, 로그만 저장 (ES 검색 결과는 안 씀)
    String combinedKeyword = Stream.of(title, content)
            .filter(s -> s != null && !s.isBlank())
            .collect(Collectors.joining(" "));

    if (!combinedKeyword.isBlank()) {
      try {
        if (userIdx != null) {
          // ✅ 먼저 관리자 여부 판단
          boolean isAdmin = userRepository.findById(userIdx)
                  .map(user -> user.getUserRole() == UserRole.ADMIN)
                  .orElse(false);

          // ✅ 일반 사용자일 경우만 로그 저장 메서드 호출
          if (!isAdmin) {
            recruitmentSearchService.search(combinedKeyword, Long.valueOf(userIdx));
          }
        } else {
          // 비로그인 사용자도 로그 저장 (userId 없이)
          recruitmentSearchService.search(combinedKeyword, null);
        }
      } catch (IOException e) {
        System.out.println("❌ Elasticsearch 검색 로그 저장 실패: " + e.getMessage());
      }
    }



    // [2] 기존 JPA 조건 검색 로직 유지
    Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);

    Specification<Recruitment> spec = Specification.where(RecruitmentSpecification.titleContains(title))
      .and(RecruitmentSpecification.contentContains(content))
      .and(RecruitmentSpecification.regionContains(region))
      .and(RecruitmentSpecification.companyContains(company))
      .and(RecruitmentSpecification.deadlineBetween(startDateStr, endDateStr));

    Specification<Recruitment> finalSpec = (root, query, cb) -> {
      Predicate basePredicate = spec.toPredicate(root, query, cb);

      if (sortOrder != null && sortOrder.toLowerCase().startsWith("scrap")) {
        var favoriteJoin = root.join("favorites", JoinType.LEFT);
        query.groupBy(root.get("recruitmentIdx"));

        if ("scrap_desc".equalsIgnoreCase(sortOrder)) {
          query.orderBy(cb.desc(cb.countDistinct(favoriteJoin.get("id"))));
        } else {
          query.orderBy(cb.asc(cb.countDistinct(favoriteJoin.get("id"))));
        }
      } else {
        switch (sortOrder != null ? sortOrder.toLowerCase() : "") {
          case "deadline_asc" -> query.orderBy(cb.asc(root.get("deadline")));
          case "deadline_desc" -> query.orderBy(cb.desc(root.get("deadline")));
          default -> query.orderBy(cb.desc(root.get("createdAt")));
        }
      }

      return basePredicate;
    };

    Page<Recruitment> recruitmentPage = recruitmentRepository.findAll(finalSpec, pageable);

    List<RecruitmentDTO> dtoList = recruitmentPage.stream()
      .map(entity -> RecruitmentDTO.builder()
        .recruitmentIdx(entity.getRecruitmentIdx())
        .title(entity.getTitle())
        .company(entity.getCompany())
        .deadline(entity.getDeadline())
        .scrapCount(entity.getFavorites() != null ? entity.getFavorites().size() : 0)
        .qualifications(entity.getQualifications())
        .logoUrl(entity.getLogoUrl())
        .responsibilities(entity.getResponsibilities())
        .preferred(entity.getPreferred())
        .benefits(entity.getBenefits())
        .location(entity.getLocation())
        .salary(entity.getSalary())
        .employmentType(entity.getEmploymentType())
        .build())
      .collect(Collectors.toList());

    return new PageImpl<>(dtoList, pageable, recruitmentPage.getTotalElements());
  }



  public RecruitmentDTO getRecruitmentDetail(Long id) {
    // RecruitmentService로 위임해도 되고 여기서 직접 처리해도 됨
    return recruitmentRepository.findById(id)
      .map(entity -> RecruitmentDTO.builder()
        .recruitmentIdx(entity.getRecruitmentIdx())
        .title(entity.getTitle())
        .company(entity.getCompany())
        .deadline(entity.getDeadline())
        .qualifications(entity.getQualifications())
        .logoUrl(entity.getLogoUrl())
        .responsibilities(entity.getResponsibilities())
        .preferred(entity.getPreferred())
        .benefits(entity.getBenefits())
        .location(entity.getLocation())
        .salary(entity.getSalary())
        .employmentType(entity.getEmploymentType())
        .build())
      .orElse(null);
  }

  public String buildPrevUrl(int page, int pageSize, String sortOrder,
                             String title, String content, String region,
                             String company, String startDate, String endDate) {
    StringBuilder sb = new StringBuilder("/empl/empl_main?page=").append(page);
    sb.append("&pageSize=").append(pageSize);

    if (sortOrder != null && !sortOrder.isEmpty()) {
      sb.append("&sortOrder=").append(URLEncoder.encode(sortOrder, StandardCharsets.UTF_8));
    }
    if (title != null && !title.isEmpty()) {
      sb.append("&title=").append(URLEncoder.encode(title, StandardCharsets.UTF_8));
    }
    if (content != null && !content.isEmpty()) {
      sb.append("&content=").append(URLEncoder.encode(content, StandardCharsets.UTF_8));
    }
    if (region != null && !region.isEmpty()) {
      sb.append("&region=").append(URLEncoder.encode(region, StandardCharsets.UTF_8));
    }
    if (company != null && !company.isEmpty()) {
      sb.append("&company=").append(URLEncoder.encode(company, StandardCharsets.UTF_8));
    }
    if (startDate != null && !startDate.isEmpty()) {
      sb.append("&startDate=").append(URLEncoder.encode(startDate, StandardCharsets.UTF_8));
    }
    if (endDate != null && !endDate.isEmpty()) {
      sb.append("&endDate=").append(URLEncoder.encode(endDate, StandardCharsets.UTF_8));
    }

    return sb.toString();
  }

}
