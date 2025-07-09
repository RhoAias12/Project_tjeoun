package com.tjoeun.service;

import com.tjoeun.document.RecruitmentSearchDocument;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentSyncService {

    private final RecruitmentRepository recruitmentRepository;
    private final ElasticsearchService elasticsearchService;

    // 전체 DB 데이터를 ES에 동기화
    public void syncAllToElasticsearch() {
        List<Recruitment> recruitments = recruitmentRepository.findAll();

        for (Recruitment r : recruitments) {
            RecruitmentSearchDocument doc = RecruitmentSearchDocument.builder()
                    .title(r.getTitle())
                    .company(r.getCompany())
                    .location(r.getLocation())
                    .deadline(r.getDeadline())
                    .qualifications(r.getQualifications())
                    .responsibilities(r.getResponsibilities())
                    .preferred(r.getPreferred())
                    .benefits(r.getBenefits())
                    .employmentType(r.getEmploymentType())
                    .build();

            elasticsearchService.saveToES(doc);
        }

        System.out.println("✅ 전체 ES 동기화 완료: " + recruitments.size() + "건");
    }
}
