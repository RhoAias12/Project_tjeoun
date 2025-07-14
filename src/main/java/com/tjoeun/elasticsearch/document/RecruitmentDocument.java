package com.tjoeun.elasticsearch.document;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class RecruitmentDocument {

  @Id
  private Long recruitmentIdx;

  // @Field(type = FieldType.Keyword)
  @Field(type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "standard")
  private String title;

  // @Field(type = FieldType.Keyword)
  @Field(type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "standard")
  private String company;

  private String deadline;
//  @Field(type = FieldType.Date, format = DateFormat.date, pattern = "yyyy-MM-dd")
//  private LocalDate deadline;

//  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
//  private LocalDateTime deadline;

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

  //  @Field(type = FieldType.Keyword)
  @Field(type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "standard")
  private String location;


  @Field(type = FieldType.Text)
  private String salary;

  @Field(type = FieldType.Keyword)
  private String employmentType;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  @Field(type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "standard")
  private String combinedContent;

  @Field(type = FieldType.Text, analyzer = "korean_custom", fielddata = true)
  private String jobKeywords;

  @Field(type = FieldType.Integer)
  private Integer scrapCount;
}
