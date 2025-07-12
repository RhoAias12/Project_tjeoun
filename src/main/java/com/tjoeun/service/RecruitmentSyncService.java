package com.tjoeun.service;

import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import com.tjoeun.elasticsearch.repository.RecruitmentSearchRepository;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecruitmentSyncService {

  private final RecruitmentRepository recruitmentRepository;
  private final RecruitmentSearchRepository recruitmentSearchRepository;

  // MariaDB 저장 후 Elasticsearch 색인 생성/갱신
  @Transactional
  public Recruitment save(Recruitment recruitment) {
    Recruitment saved = recruitmentRepository.save(recruitment);

    RecruitmentDocument doc = mapToDocument(saved);
    recruitmentSearchRepository.save(doc);

    return saved;
  }

  // MariaDB 삭제 후 Elasticsearch 색인 삭제
  @Transactional
  public void delete(Long recruitmentIdx) {
    recruitmentRepository.deleteById(recruitmentIdx);
    recruitmentSearchRepository.deleteById(recruitmentIdx);
  }

  private RecruitmentDocument mapToDocument(Recruitment recruitment) {
    return RecruitmentDocument.builder()
      .recruitmentIdx(recruitment.getRecruitmentIdx())
      .title(recruitment.getTitle())
      .company(recruitment.getCompany())
      .deadline(safeFormatDeadline(recruitment.getDeadline()))
      .qualifications(recruitment.getQualifications())
      .logoUrl(recruitment.getLogoUrl())
      .responsibilities(recruitment.getResponsibilities())
      .preferred(recruitment.getPreferred())
      .benefits(recruitment.getBenefits())
      .location(recruitment.getLocation())
      .salary(recruitment.getSalary())
      .employmentType(recruitment.getEmploymentType())
      .createdAt(recruitment.getCreatedAt())
      .combinedContent(String.join(" ",
        Optional.ofNullable(recruitment.getQualifications()).orElse(""),
        Optional.ofNullable(recruitment.getResponsibilities()).orElse(""),
        Optional.ofNullable(recruitment.getPreferred()).orElse(""),
        Optional.ofNullable(recruitment.getBenefits()).orElse(""),
        Optional.ofNullable(recruitment.getSalary()).orElse(""),
        Optional.ofNullable(recruitment.getEmploymentType()).orElse(""))
      )
      .scrapCount(recruitment.getFavorites() != null ? recruitment.getFavorites().size() : 0)
      .build();
  }

  private LocalDateTime safeFormatDeadline(Object deadline) {
    try {
      if (deadline instanceof LocalDateTime) {
        return (LocalDateTime) deadline;
      } else if (deadline instanceof LocalDate) {
        return ((LocalDate) deadline).atStartOfDay();
      } else if (deadline instanceof String) {
        // 예: "9999" 같은 잘못된 문자열이면 null 반환
        return null;
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }
}
