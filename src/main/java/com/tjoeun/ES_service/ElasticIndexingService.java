package com.tjoeun.ES_service;

import com.tjoeun.ES_repository.RecruitmentElasticRepository;
import com.tjoeun.document.RecruitmentElastic;
import com.tjoeun.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticIndexingService {

    private final RecruitmentRepository recruitmentRepo; // JPA
    private final RecruitmentElasticRepository elasticRepo;

    public void syncAllToElasticsearch() {
        List<RecruitmentElastic> documents = recruitmentRepo.findAll().stream()
                .map(r -> RecruitmentElastic.builder()
                        .id(r.getRecruitmentIdx())
                        .title(r.getTitle())
                        .description(r.getResponsibilities())
                        .region(r.getLocation())
                        .company(r.getCompany())
                        .startDate(r.getCreatedAt().toLocalDate())
                        .deadline(r.getDeadline())
                        .build())
                .toList();

        elasticRepo.saveAll(documents);
    }


}
