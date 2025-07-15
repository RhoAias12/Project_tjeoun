package com.tjoeun.elasticsearch.sync;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import com.tjoeun.elasticsearch.repository.RecruitmentSearchRepository;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.RecruitmentRepository;
import com.tjoeun.service.ESIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecruitmentSyncService {

  private final RecruitmentRepository recruitmentRepository;
  private final RecruitmentSearchRepository recruitmentSearchRepository;
  private final FavoriteRepository favoriteRepository;
  private final ElasticsearchClient esClient;
  private final ESIndexService esIndexService;
  private static final String INDEX_NAME = "recruitments";


  // MariaDB 저장 후 Elasticsearch 색인 생성/갱신
  @Transactional
  public Recruitment save(Recruitment recruitment) throws IOException {
    Recruitment saved = recruitmentRepository.save(recruitment);
    RecruitmentDocument doc = mapToDocument(saved);

    esClient.index(i -> i
      .index(INDEX_NAME)
      .id(String.valueOf(doc.getRecruitmentIdx()))
      .document(doc)
    );

    return saved;
  }

  @Transactional
  public void delete(Long recruitmentIdx) throws IOException {
    recruitmentRepository.deleteById(recruitmentIdx);

    esClient.delete(d -> d
      .index(INDEX_NAME)
      .id(String.valueOf(recruitmentIdx))
    );
  }

  private RecruitmentDocument mapToDocument(Recruitment recruitment) {
    int scrapCount = favoriteRepository.countByRecruitment(recruitment);

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
      .createdAt(
        Optional.ofNullable(recruitment.getCreatedAt())
          .orElse(LocalDateTime.now())
      )
      .combinedContent(String.join(" ",
        Optional.ofNullable(recruitment.getQualifications()).orElse(""),
        Optional.ofNullable(recruitment.getResponsibilities()).orElse(""),
        Optional.ofNullable(recruitment.getPreferred()).orElse(""),
        Optional.ofNullable(recruitment.getBenefits()).orElse(""),
        Optional.ofNullable(recruitment.getSalary()).orElse(""),
        Optional.ofNullable(recruitment.getEmploymentType()).orElse(""))
      )
      .scrapCount(scrapCount)
      .build();
  }

  private String safeFormatDeadline(LocalDateTime deadline) {
    try {
      return deadline != null ? deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) : null;
    } catch (Exception e) {
      return null;
    }
  }

}
