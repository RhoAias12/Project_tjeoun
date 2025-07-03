package com.tjoeun.service;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.repository.RecruitmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RecruitmentService {

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    public List<RecruitmentDTO> getAllPosts() {
        return recruitmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RecruitmentDTO getPostById(Long id) {
        return recruitmentRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public Page<RecruitmentDTO> getPagedPosts(Pageable pageable) {
        return recruitmentRepository.findAll(pageable)
                .map(this::convertToDTO);
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
                .build();
    }



    public Page<RecruitmentDTO> getSortedPagedPosts(Pageable pageable, String favoriteSort, String deadlineSort) {
        List<Recruitment> recruitments = recruitmentRepository.findAll();

        Stream<Recruitment> stream = recruitments.stream();

        if ("favorite_all".equals(favoriteSort) && "deadline_all".equals(deadlineSort)) {
            // 둘 다 전체면 기본 정렬 (recruitmentIdx 오름차순)
            stream = stream.sorted(Comparator.comparing(Recruitment::getRecruitmentIdx));
        } else {
            Comparator<Recruitment> comparator = Comparator.comparing(Recruitment::getDeadline);

            if ("deadline_desc".equals(deadlineSort)) {
                comparator = Comparator.comparing(Recruitment::getDeadline).reversed();
            } else if ("deadline_all".equals(deadlineSort)) {
                // deadline 전체일 경우 마감일 기준 정렬 없이 기본 정렬로 할지 아니면 deadline 오름차순 유지할지 정할 수 있음
                // 여기선 기본 정렬로 함
                comparator = Comparator.comparing(Recruitment::getRecruitmentIdx);
            }

            if ("favorite_desc".equals(favoriteSort)) {
                comparator = Comparator
                  .comparing((Recruitment r) -> -r.getFavorites().size())
                  .thenComparing(comparator);
            } else if ("favorite_asc".equals(favoriteSort)) {
                comparator = Comparator
                  .comparing((Recruitment r) -> r.getFavorites().size())
                  .thenComparing(comparator);
            } else if ("favorite_all".equals(favoriteSort)) {
                // favorite 전체일 경우 마감일 정렬만 적용
                if (!"deadline_all".equals(deadlineSort)) {
                    // deadline 정렬만 적용
                    if ("deadline_desc".equals(deadlineSort)) {
                        comparator = Comparator.comparing(Recruitment::getDeadline).reversed();
                    } else {
                        comparator = Comparator.comparing(Recruitment::getDeadline);
                    }
                } else {
                    comparator = Comparator.comparing(Recruitment::getRecruitmentIdx);
                }
            }

            stream = stream.sorted(comparator);
        }

        List<RecruitmentDTO> sortedList = stream
          .map(this::convertToDTO)
          .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedList.size());
        List<RecruitmentDTO> pageContent = sortedList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, sortedList.size());
    }


}
