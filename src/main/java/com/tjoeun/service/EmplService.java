package com.tjoeun.service;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.RecruitmentRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmplService {

  private final RecruitmentRepository recruitmentRepository;

  public Page<RecruitmentDTO> getJobPage(
    int page,
    int pageSize,
    String sortOrder,
    String title,
    String content,
    String region,
    String company,
    String startDateStr,
    String endDateStr) {

    Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);

    Specification<Recruitment> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 필터 조건
      if (title != null && !title.isBlank()) {
        predicates.add(cb.like(root.get("title"), "%" + title + "%"));
      }
      if (content != null && !content.isBlank()) {
        Predicate contentPredicate = cb.or(
          cb.like(cb.lower(root.get("qualifications")), "%" + content.toLowerCase() + "%"),
          cb.like(cb.lower(root.get("responsibilities")), "%" + content.toLowerCase() + "%"),
          cb.like(cb.lower(root.get("preferred")), "%" + content.toLowerCase() + "%"),
          cb.like(cb.lower(root.get("benefits")), "%" + content.toLowerCase() + "%"),
          cb.like(cb.lower(root.get("salary")), "%" + content.toLowerCase() + "%"),
          cb.like(cb.lower(root.get("employmentType")), "%" + content.toLowerCase() + "%")
        );
        predicates.add(contentPredicate);
      }
      if (region != null && !region.isBlank()) {
        predicates.add(cb.like(root.get("location"), "%" + region + "%"));
      }
      if (company != null && !company.isBlank()) {
        predicates.add(cb.like(root.get("company"), "%" + company + "%"));
      }
      if (startDateStr != null && !startDateStr.isBlank() &&
        endDateStr != null && !endDateStr.isBlank()) {
        try {
          LocalDate startDate = LocalDate.parse(startDateStr);
          LocalDate endDate = LocalDate.parse(endDateStr);
          predicates.add(cb.between(root.get("deadline"), startDate, endDate));
        } catch (Exception ignored) {}
      }

      // 정렬 조건에 따른 join, groupBy, orderBy 처리
      if (sortOrder != null && sortOrder.toLowerCase().startsWith("scrap")) {
        var favoriteJoin = root.join("favorites", JoinType.LEFT);
        query.groupBy(root.get("id"));

        if ("scrap_desc".equalsIgnoreCase(sortOrder)) {
          query.orderBy(cb.desc(cb.countDistinct(favoriteJoin.get("id"))));
        } else {
          query.orderBy(cb.asc(cb.countDistinct(favoriteJoin.get("id"))));
        }
      } else {
        // 스크랩 정렬이 아닌 경우
        switch (sortOrder != null ? sortOrder.toLowerCase() : "") {
          case "deadline_asc" -> query.orderBy(cb.asc(root.get("deadline")));
          case "deadline_desc" -> query.orderBy(cb.desc(root.get("deadline")));
          default -> query.orderBy(cb.desc(root.get("createdAt")));
        }
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Recruitment> recruitmentPage = recruitmentRepository.findAll(spec, pageable);

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
      .toList();

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
