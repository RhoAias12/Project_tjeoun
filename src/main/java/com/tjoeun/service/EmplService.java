package com.tjoeun.service;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmplService {

  private final RecruitmentRepository recruitmentRepository;

  // DB에서 필터 + 정렬 + 페이징 처리
  public Page<RecruitmentDTO> getJobPage(
    int page,
    int pageSize,
    String scrapSort,
    String deadlineSort,
    String title,
    String content,
    String region,
    String company,
    String startDateStr,
    String endDateStr) {

    // 정렬 조건 만들기
    List<Sort.Order> orders = new ArrayList<>();

    // scrapSort 정렬은 DB에 없으면 일단 무시하고 deadlineSort 위주로 처리
    if ("deadline-asc".equalsIgnoreCase(deadlineSort)) {
      orders.add(Sort.Order.asc("deadline"));
    } else if ("deadline-desc".equalsIgnoreCase(deadlineSort)) {
      orders.add(Sort.Order.desc("deadline"));
    } else {
      orders.add(Sort.Order.desc("createdAt")); // 기본 정렬
    }

    Sort sort = Sort.by(orders);

    // Pageable 객체
    Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, sort);

    // Specification(동적 쿼리) 생성
    Specification<Recruitment> spec = (root, query, cb) -> {
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

      if (startDateStr != null && !startDateStr.isBlank() && endDateStr != null && !endDateStr.isBlank()) {
        try {
          LocalDate startDate = LocalDate.parse(startDateStr);
          LocalDate endDate = LocalDate.parse(endDateStr);
          predicates.add(cb.between(root.get("deadline"), startDate, endDate));
        } catch (DateTimeParseException e) {
          // 날짜 파싱 오류 시 무시하거나 로그 처리
        }
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Recruitment> recruitmentPage = recruitmentRepository.findAll(spec, pageable);

    // 엔티티 -> DTO 변환 (scrapCount 등 추가는 필요시 따로 처리)
    List<RecruitmentDTO> dtoList = recruitmentPage.stream()
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
        // scrapCount 등 추가 처리 필요하면 여기서 추가
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
}
