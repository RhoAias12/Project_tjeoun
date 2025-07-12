package com.tjoeun.document;

import co.elastic.clients.json.JsonData;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDate;

@Document(indexName = "recruitments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentSearchDocument {


    private String title;
    private String job;
    private String company;
    private String location;
    private LocalDate deadline;
    private String employmentType;
    private String qualifications;
    private String responsibilities;
    private String preferred;
    private String benefits;
}
