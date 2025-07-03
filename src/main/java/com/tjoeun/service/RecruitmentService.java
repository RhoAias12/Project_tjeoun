package com.tjoeun.service;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecruitmentService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    public List<RecruitmentDTO> getAllPosts() {
        return jobPostingRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 🔽 customSort에 따른 정렬 리스트 반환
    public List<RecruitmentDTO> getAllPosts(String customSort) {
        List<Recruitment> list = jobPostingRepository.findAll();

        // scrapCount 채우기
        for (Recruitment r : list) {
            int count = favoriteRepository.countByRecruitment(r);
            r.setScrapCount(count);
        }

        if ("scrap-desc".equals(customSort)) {
            // 스크랩 된 것만 필터링
            list = list.stream()
                    .filter(r -> r.getScrapCount() != null && r.getScrapCount() > 0)
                    .collect(Collectors.toList());
            list.sort(Comparator.comparing(Recruitment::getScrapCount).reversed());

        } else if ("scrap-asc".equals(customSort)) {
            // 스크랩 안 된 것만 필터링
            list = list.stream()
                    .filter(r -> r.getScrapCount() == null || r.getScrapCount() == 0)
                    .collect(Collectors.toList());
        }


        // 정렬
        if ("scrap-desc".equals(customSort)) {
            list.sort(Comparator.comparing(Recruitment::getScrapCount).reversed());
        } else if ("scrap-asc".equals(customSort)) {
            list.sort(Comparator.comparing(Recruitment::getScrapCount));
        } else if ("deadline-asc".equals(customSort)) {
            list.sort(Comparator.comparing(Recruitment::getDeadline));
        } else if ("deadline-desc".equals(customSort)) {
            list.sort(Comparator.comparing(Recruitment::getDeadline).reversed());
        }

        return list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    public Page<RecruitmentDTO> getPagedPosts(Pageable pageable) {
        return jobPostingRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    // 🔽 customSort에 따른 정렬 Page 반환
    public Page<RecruitmentDTO> getPagedPosts(Pageable pageable, String customSort) {
        Sort sort = resolveSort(customSort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return jobPostingRepository.findAll(sortedPageable)
                .map(this::convertToDTO);
    }

    // 🔽 정렬 조건 처리 메서드
    private Sort resolveSort(String customSort) {
        if (customSort == null || customSort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt"); // 기본 정렬
        }

        return switch (customSort) {
            case "deadline-asc" -> Sort.by(Sort.Direction.ASC, "deadline");
            case "deadline-desc" -> Sort.by(Sort.Direction.DESC, "deadline");
            // ⚠️ scrap 관련 정렬은 제거해야 함
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }


    private RecruitmentDTO convertToDTO(Recruitment entity) {
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
                .scrapCount(entity.getScrapCount())
                .build();
    }

    public RecruitmentDTO getPostById(Long id) {
        return jobPostingRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }


}
