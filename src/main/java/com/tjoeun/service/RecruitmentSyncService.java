package com.tjoeun.service;

import com.tjoeun.document.RecruitmentSearchDocument;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import com.tjoeun.elasticsearch.repository.RecruitmentSearchRepository;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tjoeun.elasticsearch.KoreanNounExtractor;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecruitmentSyncService {

  private final RecruitmentRepository recruitmentRepository;
  private final RecruitmentSearchRepository recruitmentSearchRepository;
  private final ElasticsearchService elasticsearchService;

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
    String combined = Optional.ofNullable(recruitment.getTitle()).orElse("") + " " +
            Optional.ofNullable(recruitment.getResponsibilities()).orElse("");

    String jobKeywords = String.join(" ", KoreanNounExtractor.extractNouns(combined));


    return RecruitmentDocument.builder()
            .recruitmentIdx(recruitment.getRecruitmentIdx())
            .title(recruitment.getTitle())
            .company(recruitment.getCompany())
            .deadline(recruitment.getDeadline())
            .qualifications(recruitment.getQualifications())
            .logoUrl(recruitment.getLogoUrl())
            .responsibilities(recruitment.getResponsibilities())
            .preferred(recruitment.getPreferred())
            .benefits(recruitment.getBenefits())
            .location(recruitment.getLocation())
            .salary(recruitment.getSalary())
            .employmentType(recruitment.getEmploymentType())
            .combinedContent(String.join(" ",
                    Optional.ofNullable(recruitment.getQualifications()).orElse(""),
                    Optional.ofNullable(recruitment.getResponsibilities()).orElse(""),
                    Optional.ofNullable(recruitment.getPreferred()).orElse(""),
                    Optional.ofNullable(recruitment.getBenefits()).orElse(""),
                    Optional.ofNullable(recruitment.getSalary()).orElse(""),
                    Optional.ofNullable(recruitment.getEmploymentType()).orElse("")
            ))
            .jobKeywords(jobKeywords)
            .build();
  }

    // 전체 DB 데이터를 ES에 동기화
    public void syncAllToElasticsearch() {
      List<Recruitment> recruitments = recruitmentRepository.findAll();

      for (Recruitment r : recruitments) {
        recruitmentSearchRepository.save(mapToDocument(r));
      }

      System.out.println(" 전체 ES 동기화 완료: " + recruitments.size() + "건");
    }

}