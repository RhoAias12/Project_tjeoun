package com.tjoeun.elasticsearch.sync;

import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import com.tjoeun.elasticsearch.repository.RecruitmentSearchRepository;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.RecruitmentRepository;
import com.tjoeun.service.ElasticsearchService;
import com.tjoeun.elasticsearch.KoreanNounExtractor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecruitmentSyncService {

  private final RecruitmentRepository recruitmentRepository;
  private final FavoriteRepository favoriteRepository;

  @Lazy
  private final RecruitmentSearchRepository recruitmentSearchRepository;

  private final ElasticsearchService elasticsearchService;

    @PostConstruct
    public void init() {
        if (recruitmentSearchRepository.count() == 0) {
            System.out.println("자동 동기화 실행됨 ✅");
            syncAllToElasticsearch();
        } else {
            System.out.println("ES에 이미 데이터 있음, 자동 동기화 생략");
        }
    }


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

    // 전체 DB 데이터를 ES에 동기화
    public void syncAllToElasticsearch() {
        List<Recruitment> recruitments = recruitmentRepository.findAllWithFavorites();
        System.out.println("불러온 DB 데이터 수: " + recruitments.size());

        int count = 0;
        for (Recruitment r : recruitments) {
            try {
                RecruitmentDocument doc = mapToDocument(r);
                System.out.println("색인할 문서: " + doc);
                recruitmentSearchRepository.save(doc);
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("전체 ES 동기화 완료 ✅ 총 색인 건수: " + count);
    }

    private RecruitmentDocument mapToDocument(Recruitment recruitment) {
        String combined = Optional.ofNullable(recruitment.getTitle()).orElse("") + " " +
                Optional.ofNullable(recruitment.getResponsibilities()).orElse("");

        String jobKeywords = String.join(" ", KoreanNounExtractor.extractNouns(combined));

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
                .createdAt(recruitment.getCreatedAt())
                .combinedContent(String.join(" ",
                        Optional.ofNullable(recruitment.getQualifications()).orElse(""),
                        Optional.ofNullable(recruitment.getResponsibilities()).orElse(""),
                        Optional.ofNullable(recruitment.getPreferred()).orElse(""),
                        Optional.ofNullable(recruitment.getBenefits()).orElse(""),
                        Optional.ofNullable(recruitment.getSalary()).orElse(""),
                        Optional.ofNullable(recruitment.getEmploymentType()).orElse(""))
                )
                .scrapCount(scrapCount)
                .jobKeywords(jobKeywords)
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

