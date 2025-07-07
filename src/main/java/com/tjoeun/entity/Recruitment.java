package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

  @Column
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

//  @OneToMany(mappedBy = "recruitment", cascade = CascadeType.ALL, orphanRemoval = true)
//  private List<Favorite> favorites;



  @OneToMany(mappedBy = "recruitment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Favorite> favorites = new ArrayList<>();

  @OneToMany(mappedBy = "recruitment", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<ApplyHistory> applyHistories = new ArrayList<>();
}
