package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "apply_history_resume_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyHistoryResumeContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "apply_history_resume_id", nullable = false)
  private ApplyHistoryResume applyHistoryResume;

  @Column(columnDefinition = "TEXT")
  private String question;

  @Column(columnDefinition = "TEXT")
  private String context;
}
