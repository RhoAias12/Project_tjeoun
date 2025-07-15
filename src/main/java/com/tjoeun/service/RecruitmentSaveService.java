package com.tjoeun.service;

import com.tjoeun.document.RecruitmentSearchDocument;
import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RecruitmentSaveService {

    private final RecruitmentRepository recruitmentRepository;
    private final ElasticsearchService elasticsearchService;

    public void saveAll(List<RecruitmentDTO> crawledList) {
        for (RecruitmentDTO dto : crawledList) {
            //  DB 저장
            Recruitment entity = Recruitment.builder()
                    .title(dto.getTitle())
                    .company(dto.getCompany())
                    .location(dto.getLocation())
                    .deadline(dto.getDeadline())
                    .qualifications(dto.getQualifications())
                    .responsibilities(dto.getResponsibilities())
                    .preferred(dto.getPreferred())
                    .benefits(dto.getBenefits())
                    .employmentType(dto.getEmploymentType())
                    .build();

            recruitmentRepository.save(entity);

            //  ES 저장
            RecruitmentSearchDocument doc = RecruitmentSearchDocument.builder()
                    .title(dto.getTitle())
                    .company(dto.getCompany())
                    .location(dto.getLocation())
                    .deadline(LocalDate.from(dto.getDeadline()))
                    .qualifications(dto.getQualifications())
                    .responsibilities(dto.getResponsibilities())
                    .preferred(dto.getPreferred())
                    .benefits(dto.getBenefits())
                    .employmentType(dto.getEmploymentType())
                    .build();

            elasticsearchService.saveToES(doc);
        }
    }
}

