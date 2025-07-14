package com.tjoeun.document;

import co.elastic.clients.json.JsonData;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Document(indexName = "recruitments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentSearchDocument {


    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String job;

    @Field(type = FieldType.Text)
    private String company;

    @Field(type = FieldType.Text)
    private String location;

    @Field(type = FieldType.Date)
    private LocalDate deadline;

    @Field(type = FieldType.Text)
    private String employmentType;

    @Field(type = FieldType.Text)
    private String qualifications;

    @Field(type = FieldType.Text)
    private String responsibilities;

    @Field(type = FieldType.Text)
    private String preferred;

    @Field(type = FieldType.Text)
    private String benefits;

}
