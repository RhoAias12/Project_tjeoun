package com.tjoeun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentDTO {
    private Long recruitmentIdx;
    private String title;
    private String company;
    private LocalDate deadline;
    private String qualifications;
    private String logoUrl;
    private String responsibilities;
    private String preferred;
    private String benefits;
    private String location;
    private String salary;
    private String employmentType;
    private Integer scrapCount;

    public RecruitmentDTO(Long recruitmentIdx, String title, String company, LocalDate deadline, Long scrapCount) {
        this.recruitmentIdx = recruitmentIdx;
        this.title = title;
        this.company = company;
        this.deadline = deadline;
        this.scrapCount = scrapCount != null ? scrapCount.intValue() : 0;
    }

    public RecruitmentDTO(com.tjoeun.entity.Recruitment entity) {
        this.recruitmentIdx = entity.getRecruitmentIdx();
        this.title = entity.getTitle();
        this.company = entity.getCompany();
        this.deadline = entity.getDeadline();
        this.qualifications = entity.getQualifications();
        this.logoUrl = entity.getLogoUrl();
        this.responsibilities = entity.getResponsibilities();
        this.preferred = entity.getPreferred();
        this.benefits = entity.getBenefits();
        this.location = entity.getLocation();
        this.salary = entity.getSalary();
        this.employmentType = entity.getEmploymentType();
        this.scrapCount = entity.getFavorites() != null ? entity.getFavorites().size() : 0;
    }
}
