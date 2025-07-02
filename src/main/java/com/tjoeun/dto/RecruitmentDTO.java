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
}
