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

  @Field(type = FieldType.Text)
  private String combinedContent;
}
