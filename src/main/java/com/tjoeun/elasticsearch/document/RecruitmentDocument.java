package com.tjoeun.elasticsearch.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "recruitments")
@Data
public class RecruitmentDocument {

  @Id
  private Long recruitmentIdx;

  private String title;
  private String company;

  private String deadline;

  private String qualifications;
  private String logoUrl;
  private String responsibilities;
  private String preferred;
  private String benefits;
  private String location;
  private String salary;
  private String employmentType;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime createdAt;

  @Field(type = FieldType.Text)
  private String combinedContent;

  @Field(type = FieldType.Integer)
  private Integer scrapCount;
}
