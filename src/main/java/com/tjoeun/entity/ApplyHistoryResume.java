package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "apply_history_resume")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyHistoryResume {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long ApplyHistoryResumeId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "apply_history_idx", nullable = false, unique = true)
  private ApplyHistory applyHistory;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_idx", nullable = false)
  private Users user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recruitment_idx", nullable = false)
  private Recruitment recruitment;

  // 메인 필드들 (Resume에서 복사)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String context;

  private String img;

  private String address;

  private String phoneNum;

  @Column(columnDefinition = "TEXT")
  private String education;

  @Column(columnDefinition = "TEXT")
  private String antecedents;

  @Column(columnDefinition = "TEXT")
  private String ability;

  @Column(columnDefinition = "TEXT")
  private String awards;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  @Builder.Default
  @OneToMany(mappedBy = "applyHistoryResume", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ApplyHistoryResumeContent> contents = new ArrayList<>();

  public void addContent(ApplyHistoryResumeContent content) {
    content.setApplyHistoryResume(this);
    this.contents.add(content);
  }
}
