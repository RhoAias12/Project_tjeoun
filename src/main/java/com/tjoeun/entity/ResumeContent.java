package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer resumeContentIdx;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resume_idx", nullable = false)
  private Resume resume;

  @Column(columnDefinition = "TEXT")
  private String question;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Column(columnDefinition = "TEXT")
  private String context;

  /** 자동 시간 관리 **/
  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

}
