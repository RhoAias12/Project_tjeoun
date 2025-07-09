package com.tjoeun.service;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.RecruitmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public List<RecruitmentDTO> getAllPosts() {
        List<Recruitment> list = recruitmentRepository.findAll();

        return list.stream()
                .map(r -> {
                    int count = favoriteRepository.countByRecruitment(r);
                    return convertToDTO(r, count);
                })
                .collect(Collectors.toList());
    }

    public RecruitmentDTO getPostById(Long id) {
        return recruitmentRepository.findById(id)
                .map(entity -> {
                    int scrapCount = favoriteRepository.countByRecruitment(entity);
                    return convertToDTO(entity, scrapCount);
                })
                .orElse(null);
    }


    // 🔽 customSort에 따른 정렬 리스트 반환

    public List<RecruitmentDTO> getAllPosts(String customSort) {
        List<Recruitment> list = recruitmentRepository.findAll();

        return list.stream()
                .map(r -> {
                    int count = favoriteRepository.countByRecruitment(r);
                    return convertToDTO(r, count); // scrapCount 반영
                })
                .sorted(getComparator(customSort)) // 아래 comparator 분리
                .collect(Collectors.toList());
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



    public Page<RecruitmentDTO> getPagedPosts(Pageable pageable) {
        List<Recruitment> recruitments = recruitmentRepository.findAll();

        List<RecruitmentDTO> dtoList = recruitments.stream()
                .map(r -> {
                    int count = favoriteRepository.countByRecruitment(r);
                    return convertToDTO(r, count);
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtoList.size());
        List<RecruitmentDTO> pageContent = dtoList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, dtoList.size());
    }


    // customSort에 따른 정렬 Page 반환
    public Page<RecruitmentDTO> getPagedPosts(Pageable pageable, String customSort) {
        List<Recruitment> recruitments = recruitmentRepository.findAll();

        List<RecruitmentDTO> dtoList = recruitments.stream()
                .map(r -> {
                    int count = favoriteRepository.countByRecruitment(r);
                    return convertToDTO(r, count);
                })
                .sorted(getComparator(customSort))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtoList.size());
        List<RecruitmentDTO> pageContent = dtoList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, dtoList.size());
    }


    // 정렬 조건 처리 메서드
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

    public Page<RecruitmentDTO> getSortedPagedPosts(Pageable pageable, String deadlineSort) {
        List<Recruitment> recruitments = recruitmentRepository.findAll();

        Comparator<Recruitment> comparator;

        if ("deadline_desc".equals(deadlineSort)) {
            comparator = Comparator.comparing(Recruitment::getDeadline).reversed();
        } else if ("deadline_asc".equals(deadlineSort)) {
            comparator = Comparator.comparing(Recruitment::getDeadline);
        } else {
            comparator = Comparator.comparing(Recruitment::getRecruitmentIdx); // recruitmentIdx 기준 기본 정렬
        }

        Stream<Recruitment> stream = recruitments.stream().sorted(comparator);

        List<RecruitmentDTO> sortedList = stream
                .map(r -> {
                    int count = favoriteRepository.countByRecruitment(r);
                    return convertToDTO(r, count);
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedList.size());
        List<RecruitmentDTO> pageContent = sortedList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, sortedList.size());
    }

    public Recruitment getEntityById(Long id) {
        return recruitmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고 없음"));
    }


}
