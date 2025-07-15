package com.tjoeun.service;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.elasticsearch.sync.RecruitmentSyncService;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.RecruitmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class RecruitmentService {

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    public RecruitmentDTO getPostById(Long id) {
        return recruitmentRepository.findById(id)
                .map(entity -> {
                    int scrapCount = favoriteRepository.countByRecruitment(entity);
                    return convertToDTO(entity, scrapCount);
                })
                .orElse(null);
    }

    private Comparator<RecruitmentDTO> getComparator(String sort) {

        if (sort == null || sort.isEmpty()) {
            return Comparator.comparing(RecruitmentDTO::getRecruitmentIdx); // 기본 정렬
        }

        return switch (sort) {
            case "scrap-desc" -> Comparator.comparing(RecruitmentDTO::getScrapCount, Comparator.nullsFirst(Integer::compareTo)).reversed();
            case "scrap-asc" -> Comparator.comparing(RecruitmentDTO::getScrapCount, Comparator.nullsFirst(Integer::compareTo));
            case "deadline-asc" -> Comparator.comparing(RecruitmentDTO::getDeadline);
            case "deadline-desc" -> Comparator.comparing(RecruitmentDTO::getDeadline).reversed();
            default -> Comparator.comparing(RecruitmentDTO::getRecruitmentIdx);
        };
    }

    private RecruitmentDTO convertToDTO(Recruitment entity, int scrapCount) {
        return RecruitmentDTO.builder()
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
                .scrapCount(scrapCount)
                .build();
    }

    public Recruitment getEntityById(Long id) {
        return recruitmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고 없음"));
    }


}
