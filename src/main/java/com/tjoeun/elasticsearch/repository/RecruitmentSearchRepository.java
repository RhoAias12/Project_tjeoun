package com.tjoeun.elasticsearch.repository;

import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecruitmentSearchRepository extends ElasticsearchRepository<RecruitmentDocument, Long> {

  // 개별 필드 검색
  List<RecruitmentDocument> findByTitleContaining(String title);
  List<RecruitmentDocument> findByCompanyContaining(String company);
  List<RecruitmentDocument> findByQualificationsContaining(String qualifications);
  List<RecruitmentDocument> findByResponsibilitiesContaining(String responsibilities);
  List<RecruitmentDocument> findByPreferredContaining(String preferred);
  List<RecruitmentDocument> findByBenefitsContaining(String benefits);
  List<RecruitmentDocument> findByLocationContaining(String location);
  List<RecruitmentDocument> findBySalaryContaining(String salary);
  List<RecruitmentDocument> findByEmploymentTypeContaining(String employmentType);
  List<RecruitmentDocument> findByCreatedAtContaining(LocalDateTime createdAt);
  List<RecruitmentDocument> findByDeadlineContaining(LocalDateTime deadline);

  // 모든 내용을 한데 모은 필드 검색
  List<RecruitmentDocument> findByCombinedContentContaining(String keyword);
}
