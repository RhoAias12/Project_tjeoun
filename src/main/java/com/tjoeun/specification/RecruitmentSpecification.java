package com.tjoeun.specification;

import com.tjoeun.entity.Recruitment;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public class RecruitmentSpecification {

  public static Specification<Recruitment> titleContains(String title) {
    return (root, query, cb) -> {
      if (title == null || title.isBlank()) return null;
      return cb.like(root.get("title"), "%" + title + "%");
    };
  }

  public static Specification<Recruitment> contentContains(String content) {
    return (root, query, cb) -> {
      if (content == null || content.isBlank()) return null;
      String lower = content.toLowerCase(Locale.ROOT);

      Predicate p1 = cb.like(cb.lower(root.get("qualifications")), "%" + lower + "%");
      Predicate p2 = cb.like(cb.lower(root.get("responsibilities")), "%" + lower + "%");
      Predicate p3 = cb.like(cb.lower(root.get("preferred")), "%" + lower + "%");
      Predicate p4 = cb.like(cb.lower(root.get("benefits")), "%" + lower + "%");
      Predicate p5 = cb.like(cb.lower(root.get("salary")), "%" + lower + "%");
      Predicate p6 = cb.like(cb.lower(root.get("employmentType")), "%" + lower + "%");

      return cb.or(p1, p2, p3, p4, p5, p6);
    };
  }

  public static Specification<Recruitment> regionContains(String region) {
    return (root, query, cb) -> {
      if (region == null || region.isBlank()) return null;
      return cb.like(root.get("location"), "%" + region + "%");
    };
  }

  public static Specification<Recruitment> companyContains(String company) {
    return (root, query, cb) -> {
      if (company == null || company.isBlank()) return null;
      return cb.like(root.get("company"), "%" + company + "%");
    };
  }

  public static Specification<Recruitment> deadlineBetween(String startDateStr, String endDateStr) {
    return (root, query, cb) -> {
      if (startDateStr == null || startDateStr.isBlank() || endDateStr == null || endDateStr.isBlank()) {
        return null;
      }
      try {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        return cb.between(root.get("deadline"), startDate, endDate);
      } catch (Exception e) {
        return null;
      }
    };
  }

  public static Specification<Recruitment> searchWithFilter(String title, String content, String region, String company, String startDateStr, String endDateStr) {
    return Specification
      .where(titleContains(title))
      .and(contentContains(content))
      .and(regionContains(region))
      .and(companyContains(company))
      .and(deadlineBetween(startDateStr, endDateStr));
  }
}
