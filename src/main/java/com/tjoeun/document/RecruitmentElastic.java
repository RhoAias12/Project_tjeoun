package com.tjoeun.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Document(indexName = "recruitment_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentElastic {
    @Id
    private Long id;

    private String title;
    private String description;
    private String region;
    private String company;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate startDate;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate deadline;
}


