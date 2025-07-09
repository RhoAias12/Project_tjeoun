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

      // 조인: 스크랩 정렬 시 필요
      if (sortOrder.toLowerCase().startsWith("scrap")) {
        root.join("favorites", JoinType.LEFT);
        query.groupBy(root.get("id")); // group by 필요 (스크랩 count)
      }

      // 필터 조건들
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

      var favoriteJoin = root.join("favorites", JoinType.LEFT);
      query.groupBy(root.get("id")); // count 사용 시 groupBy 필수

      switch (sortOrder.toLowerCase()) {
        case "scrap_desc" -> query.orderBy(cb.desc(cb.countDistinct(favoriteJoin.get("id"))));
        case "scrap_asc" -> query.orderBy(cb.asc(cb.countDistinct(favoriteJoin.get("id"))));
        case "deadline_asc" -> query.orderBy(cb.asc(root.get("deadline")));
        case "deadline_desc" -> query.orderBy(cb.desc(root.get("deadline")));
        default -> query.orderBy(cb.desc(root.get("createdAt")));
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
        .scrapCount(entity.getFavorites().size())
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

  public Page<RecruitmentDTO> getJobPageWithScrapFilter(
    int page, int pageSize, String sortOrder,
    String title, String content, String region,
    String company, String startDateStr, String endDateStr) {

    Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);

    Specification<Recruitment> spec = (root, query, cb) -> {
      var favoriteJoin = root.join("favorites", JoinType.LEFT);
      query.groupBy(root.get("id")); // groupBy 없으면 count로 정렬 불가능

      if ("scrap_desc".equalsIgnoreCase(sortOrder)) {
        query.orderBy(cb.desc(cb.countDistinct(favoriteJoin.get("id"))));
      } else if ("scrap_asc".equalsIgnoreCase(sortOrder)) {
        query.orderBy(cb.asc(cb.countDistinct(favoriteJoin.get("id"))));
      }

      // 필터 조건들
      List<Predicate> predicates = new ArrayList<>();

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
        } catch (DateTimeParseException ignored) {}
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    // 이렇게 하면 동적 필터 + 스크랩 정렬이 가능함
    Page<Recruitment> pageResult = recruitmentRepository.findAll(spec, pageable);

    // DTO 변환
    List<RecruitmentDTO> dtoList = pageResult.stream().map(entity -> {
      // 스크랩 수는 entity에 직접 없으니, 별도 쿼리 or 캐시 필요
      return RecruitmentDTO.builder()
        .recruitmentIdx(entity.getRecruitmentIdx())
        .title(entity.getTitle())
        .company(entity.getCompany())
        .deadline(entity.getDeadline())
        .scrapCount(null) // 나중에 따로 세팅
        .build();
    }).toList();

    return new PageImpl<>(dtoList, pageable, pageResult.getTotalElements());
  }

}
