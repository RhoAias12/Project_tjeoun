package com.tjoeun.elasticsearch.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

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

  public String getCombinedContent() {
    return String.join(" ",
      Optional.ofNullable(qualifications).orElse(""),
      Optional.ofNullable(responsibilities).orElse(""),
      Optional.ofNullable(preferred).orElse(""),
      Optional.ofNullable(benefits).orElse(""),
      Optional.ofNullable(salary).orElse(""),
      Optional.ofNullable(employmentType).orElse("")
    );
  }
}
