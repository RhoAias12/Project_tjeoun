package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "recruitment", uniqueConstraints = {
  @UniqueConstraint(columnNames = {"title", "company", "deadline"})
})
@Getter
@Setter
@NoArgsConstructor
public class Recruitment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long recruitmentIdx;

  @Column(columnDefinition = "TEXT")
  private String title;

  @Column(columnDefinition = "TEXT")
  private String company;

  @Column
  private LocalDate deadline;

  @Column(columnDefinition = "TEXT")
  private String qualifications;

  @Column(columnDefinition = "TEXT")
  private String logoUrl;

  @Column(columnDefinition = "TEXT")
  private String responsibilities;

  @Column(columnDefinition = "TEXT")
  private String preferred;

  @Column(columnDefinition = "TEXT")
  private String benefits;

  @Column(columnDefinition = "TEXT")
  private String location;

  @Column(columnDefinition = "TEXT")
  private String salary;

  @Column(columnDefinition = "TEXT")
  private String employmentType;

  @Transient
  private Integer scrapCount;

}
