package com.tjoeun.elasticsearch.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "recruitments")
public class RecruitmentDocument {

  @Id
  private Long recruitmentIdx;

  @Field(type = FieldType.Keyword)
  private String title;

  @Field(type = FieldType.Keyword)
  private String company;

  @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
  private LocalDate deadline;

  @Field(type = FieldType.Text)
  private String qualifications;

  @Field(type = FieldType.Keyword)
  private String logoUrl;

  @Field(type = FieldType.Text)
  private String responsibilities;

  @Field(type = FieldType.Text)
  private String preferred;

  @Field(type = FieldType.Text)
  private String benefits;

  @Field(type = FieldType.Keyword)
  private String location;

  @Field(type = FieldType.Text)
  private String salary;

  @Field(type = FieldType.Keyword)
  private String employmentType;

  @Field(type = FieldType.Text)
  private String combinedContent;

  @Field(type = FieldType.Text, analyzer = "korean_custom", fielddata = true)
  private String jobKeywords;
}

