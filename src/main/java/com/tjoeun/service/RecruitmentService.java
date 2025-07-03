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
          .map(this::convertToDTO)
          .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedList.size());
        List<RecruitmentDTO> pageContent = sortedList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, sortedList.size());
    }




}
